package css.cecprototype2.main;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.view.PreviewView;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import css.cecprototype2.analysis_logic.ChemicalAnalysis;
import css.cecprototype2.analysis_logic.SheetWriter;
import css.cecprototype2.region_logic.Region;
import css.cecprototype2.region_logic.RegionFinder;

public class MainViewModel extends AndroidViewModel {

    private SensorCamera cam;
    public Bitmap calibrationBitMap, analysisBitMap;
    Application application;
    RegionFinder regionFinder;
    SheetWriter sheetWriter;
    ChemicalAnalysis chemicalAnalysis;
    public List<Region> regions;
    public List<Double> calibrationIntensities;
    public List<Double> analysisIntensities;


    public MainViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        regions = new ArrayList<>();
        regionFinder = new RegionFinder(application.getApplicationContext());
        sheetWriter = new SheetWriter(application);
        chemicalAnalysis = new ChemicalAnalysis();
    }
    public Bitmap getCalibrationBitmap()
    {
        return this.calibrationBitMap;
    }
    public Bitmap getAnalysisBitmap()
    {
        return this.analysisBitMap;
    }

    public Bitmap retrieveCalibrationBitmapFromCamera()
    {
        calibrationBitMap = cam.currentBitmap;
        return this.calibrationBitMap;
    }

    public void doCalibration(){
        //calibrationBitMap = cam.currentBitmap;
        setupStandardRegions();
        Log.i("MainViewModel", ".doCalibration(): Calibration Bitmap H: " + calibrationBitMap.getHeight() + " | Calibration Bitmap W: " + calibrationBitMap.getWidth());
        calibrationIntensities = chemicalAnalysis.Calibrate(regionFinder, sheetWriter, calibrationBitMap);
    }

    public Bitmap retrieveAnalysisBitmapFromCamera()
    {
        analysisBitMap = cam.currentBitmap;
        return this.analysisBitMap;
    }

    public void doAnalysis(){
        if (chemicalAnalysis.calibrateCompleted) {
            //analysisBitMap = cam.currentBitmap;;
            setupStandardRegions();
            Log.i("MainViewModel", ".doAnalysis(): Analysis Bitmap H: " + analysisBitMap.getHeight() + " | Analysis Bitmap W: " + analysisBitMap.getWidth());
            analysisIntensities = chemicalAnalysis.Analyze(regionFinder, sheetWriter, analysisBitMap);
        } else {
            Log.i("MainViewModel", "Must do calibration before analsysis");
        }
    }



    /**
     * calls regionFinder.findRegions to get all regions in a given image
     * @return a list of regions in the image
     */
    public List<Region> setupStandardRegions() //convert image into a list of regions
    {
        regions = regionFinder.getStandardRegions();
        return regions;
    }


    public void initializeCamera(SensorCamera sensorCamera) {
        Log.i("CIS4444","MainViewModel --- initializeCamera");
        cam = sensorCamera;
    }

    public void setCameraPreview(PreviewView preview) {
        Log.i("CIS4444","MainViewModel --- setCameraPreview cam = "+cam.toString());
        cam.previewView = preview;
        //cam.startCameraProvider();
        cam.updateCameraPreview(preview);
    }

    public void takePhoto() {
        Log.i("CIS4444","MainViewModel --- takePhoto");
        //commented out for testing -- use a bitmap of sample_image_a instead of capturing an image
        cam.capturePhotoBitmap();
        //Bitmap bitmap = BitmapFactory.decodeResource(application.getResources(), R.drawable.sample_image_a);
        //Log.i("MainViewModel", ".takePhoto(): Standard Bitmap H: " + bitmap.getHeight() + " | Standard Bitmap W: " + bitmap.getWidth());
        //return bitmap;
    }

    public void setCalibrationBitMap(Bitmap sampleBitmap) {
        calibrationBitMap = sampleBitmap;
    }

    public void setAnalysisBitMap(Bitmap sampleBitmap) {
        analysisBitMap = sampleBitmap;
    }

    // TODO: This is causing an error

    public LiveData<Boolean> getBitmapAvailableLiveData() {
        Log.i("CIS4444","MainViewModel --- getBitmapAvailableLiveData = "+ cam.getAvailableLiveData().toString());
        return cam.getAvailableLiveData();
    }

}
