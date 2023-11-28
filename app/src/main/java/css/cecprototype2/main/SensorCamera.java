package css.cecprototype2.main;

import static androidx.camera.core.ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.core.content.ContextCompat;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import android.graphics.Bitmap;
import android.media.Image;
import android.view.Surface;

import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;

public class SensorCamera {
    private MutableLiveData<Boolean> bitmapAvailableLiveData;

    // Constructor: initialize camera
    public SensorCamera(Activity mainActivity, LifecycleOwner  lifecycleOwner) {
        // should be passed the application context which is needed by the camera
        // should also be passed the previewView on the screen where the image should be displayed
        // TODO: is there a better way to connect the camera to the previewView?  Will the previewView change when the phone is rotated?
        bitmapAvailableLiveData = new MutableLiveData<>();
        context = mainActivity;
        this.lifecycleOwner = lifecycleOwner;
        startCameraProvider(context,  previewView);
    }

    private Executor executor = Executors.newSingleThreadExecutor();
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    //Context context;        // the app context we are running in
    LifecycleOwner  lifecycleOwner;
    Context context;
    PreviewView previewView;
    ImageCapture imageCapture;
    ImageAnalysis imageAnalysis;
    Preview imagePreview;
    Bitmap currentBitmap;      // Bitmap from the image proxy
    Image currentImage;        // Image from the image proxy

    private void startCameraProvider(Context activityContext, PreviewView previewView) {

        cameraProviderFuture = ProcessCameraProvider.getInstance(activityContext);
        //cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        //cameraProviderFuture = ProcessCameraProvider.getInstance(context).get();

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider, previewView);
                //bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
                Log.i("CIS4444","startCameraProvider --- cameraProviderFuture ERROR " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(activityContext));
    }

    public LiveData<Boolean> getAvailableLiveData() {
        return bitmapAvailableLiveData;
    }

    private void startCameraX(@NonNull ProcessCameraProvider cameraProvider, PreviewView previewView){
        //Camera Selector Use Case
        cameraProvider.unbind();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Preview Use Case
        imagePreview = new Preview.Builder().build();
        imagePreview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image Analysis Use Case
        imageAnalysis = new ImageAnalysis.Builder()
                // enable the following line if RGBA output is needed.
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                //.setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        // Create ImageCapture builder and set manual camera settings
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setFlashMode(ImageCapture.FLASH_MODE_OFF)
                .setTargetRotation(Surface.ROTATION_0)  // Set the desired rotation
                .build();

        // Now bind all these item to the camera
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis, imageCapture, imagePreview);
    }

    public Image capturePhotoImage() {
        // public abstraction to take photo.
        // Currently calls analyze photo, but could capture photo to save to disk later...
        // The UI should call this through the MainViewModel

        capturePhotoProvider();
        analyzePhotoProvider();
        return currentImage;
    }

    public Bitmap capturePhotoBitmap() {
        // public abstraction to take photo.
        // Currently calls analyze photo, but could capture photo to save to disk later...
        // The UI should call this through the MainViewModel

        capturePhotoProvider();
        analyzePhotoProvider();
        return currentBitmap;
    }

    /**
     *  Use the camera analyze method to get an image from the camera without saving it to a file
     */
    private void analyzePhotoProvider() {
        Log.i("CIS4444","Trying to Analyze Photo --- 111");
        executor = Executors.newSingleThreadExecutor();
        Log.i("CIS4444","Trying to Analyze Photo --- 222");
        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @OptIn(markerClass = ExperimentalGetImage.class) @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                Log.i("CIS4444","Trying to Analyze Photo --- analyze callback 1");
                int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                // Get the image proxy plane's buffer which is where the pixels are
                ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
                ByteBuffer buffer = planes[0].getBuffer();
                // Create a blank bitmap
                Log.i("CIS4444","analyze callback 2");
                // cuttentBitmap = imageProxy.toBitmap();

                // TODO: find out why this no loger works to get the bitmap

                currentImage = imageProxy.getImage();

                //bitmap = Bitmap.createBitmap(imageProxy.getWidth(),imageProxy.getHeight(),Bitmap.Config.ARGB_8888);
                // copy the image proxy plane into the bitmap
                //bitmap.copyPixelsFromBuffer(buffer);
                Log.i("CIS4444", "analyze callback 2 --- bmp height = "+ currentBitmap.getHeight());

                // TODO: add code to crop to just the needed area of the photo
                //bitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height())

                bitmapAvailableLiveData.postValue(true);

                // after done, release the ImageProxy object
                imageProxy.close();
            }
        });
    }


    /**
     *  Use the camera Capture method to save an image from the camera to a file
     */
    private void capturePhotoProvider() {
        Log.i("CIS4444","Trying to Capture Photo");

        String name = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image");
        }

        // Create output options object which contains file + metadata
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(
                context.getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues).build();

        // Notify observers that the bitmap is not available
        bitmapAvailableLiveData.postValue(false);

        imageCapture.takePicture(
                outputFileOptions,
                executor,
                new ImageCapture.OnImageSavedCallback () {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Log.i("CIS4444","onImageSaved -- Photo has been taken and saved");
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException error) {
                        Log.i("CIS4444","onImageSaved -- onError");
                        error.printStackTrace();
                    }
                });

    }

}
