package com.example.rocketreserver

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.FileUpload
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.create
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.example.rocketreserver.databinding.LaunchListFragmentBinding
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.file.Paths
import java.util.concurrent.Executors

class LaunchListFragment : Fragment() {
    private lateinit var binding: LaunchListFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apolloClient = ApolloClient.builder()
            .serverUrl("https://dfunds.herokuapp.com/graphql")
            .build()

        lifecycleScope.launchWhenResumed {
            val projects = apolloClient.query(ProjectsQuery()).await()

            Log.d("Dfunds", "Success ${projects.data}")
        }

//        // Declaring a Bitmap local
//        var mImage: Bitmap?
//
//        // Declaring a webpath as a string
//        val mWebPath = "https://media.geeksforgeeks.org/wp-content/uploads/20210224040124/JSBinCollaborativeJavaScriptDebugging6-300x160.png"
//
//        // Declaring and initializing an Executor and a Handler
//        val myExecutor = Executors.newSingleThreadExecutor()
//        val myHandler = Handler(Looper.getMainLooper())
//
//        // When Button is clicked, executor will
//        // fetch the image and handler will display it.
//        // Once displayed, it is stored locally
//        myExecutor.execute {
//            mImage = mLoad(mWebPath)
//            myHandler.post {
//                if(mImage!=null){
//                    mSaveMediaToStorage(mImage)
//                }
//            }
//        }

        val createBeneficiaryMutation = CreateBeneficiaryMutation(file = FileUpload.create("image/jpeg", "/storage/emulated/0/Pictures/1658390904549.jpg"))

        apolloClient
            .mutate(createBeneficiaryMutation)
            .enqueue(object: ApolloCall.Callback<CreateBeneficiaryMutation.Data>() {
                override fun onResponse(response: Response<CreateBeneficiaryMutation.Data>) {
                    Log.d("Dfunds", response.toString());
                }

                override fun onFailure(exception: ApolloException) {
                    Log.d("Dfunds", exception.toString(), exception);
                }
            }
            )

//        lifecycleScope.launchWhenResumed {
//            val beneficiaries = apolloClient.query(BeneficiariesQuery()).await()
//
//            Log.d("Dfunds", "Success ${beneficiaries.data}")
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LaunchListFragmentBinding.inflate(inflater)
        return binding.root
    }

    // Function to establish connection and load image
    private fun mLoad(string: String): Bitmap? {
        val url: URL = mStringToURL(string)!!
        val connection: HttpURLConnection?
        try {
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val inputStream: InputStream = connection.inputStream
            val bufferedInputStream = BufferedInputStream(inputStream)
            return BitmapFactory.decodeStream(bufferedInputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    // Function to convert string to URL
    private fun mStringToURL(string: String): URL? {
        try {
            return URL(string)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return null
    }

    // Function to save image on the device.
    // Refer: https://www.geeksforgeeks.org/circular-crop-an-image-and-save-it-to-the-file-in-android/
    private fun mSaveMediaToStorage(bitmap: Bitmap?) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requireActivity().contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            Log.d("file", image.getAbsolutePath())
        }
    }
}
