package com.example.recognize;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import libsvm.*;


public class MainActivity extends AppCompatActivity implements SensorEventListener{

    SensorManager sensorManager;
    Sensor accelerometer;
    String sensorInput="";
    svm_model model;
    TextView tv;
    TextView tv2;
    String[] list;
    int count1,count2,flag=-1;long lastUpdate=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_GAME);
        tv= (TextView) findViewById(R.id.textView);
        tv2= (TextView) findViewById(R.id.textView2);
        count1=0;
        count2=0;

    }

    static {
        System.loadLibrary("native-lib");
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];
        long curTime = System.currentTimeMillis();

        if ((curTime - lastUpdate) > 100) {
            if(count1<100&&flag==0){
                count1++;
                sensorInput=sensorInput+x+" "+y+" "+z+" ";
                String current;
                current=x+" "+y+" "+z+"\n";

                Log.d("tig","onSensorChanged:"+count1+"//"+current);
            }else {if(count1>=100){
                Log.d("lol", "onSensorChanged: ");
                count1=0;
                sensorManager.unregisterListener(this);
            }

            }
            lastUpdate = curTime;
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    public void dope()
    {
        model=train();
    }



    public svm_model train() {
        int record_size=3000;
        double node_values[][] = new double[record_size][];
        int node_indexes[][] = new int[record_size][];
        double node_class_labels [] = new double[record_size];
        for (int i=0;i<1000;i++){
            node_class_labels[i]=1;
        }
        for (int i=1000;i<2000;i++){
            node_class_labels[i]=2;
        }
        for (int i=2000;i<3000;i++){
            node_class_labels[i]=3;
        }
        for(int i=0;i<3000;i++){
            node_indexes[i]=new int[3];
            node_indexes[i][0]=1;
            node_indexes[i][1]=2;
            node_indexes[i][2]=3;
        }
        for(int i=0;i<3000;i++){
            node_values[i]=new double[3];
            node_values[i][0]=Double.valueOf(list[4*i+1].substring(2));
            node_values[i][1]=Double.valueOf(list[4*i+2].substring(2));
            node_values[i][2]=Double.valueOf(list[4*i+3].substring(2));
        }
        svm_problem prob= new svm_problem();
        int dataCount =record_size;
        prob.y = new double[dataCount];
        prob.l = dataCount;
        prob.x = new svm_node[dataCount][];
        for (int i = 0; i < dataCount; i++)
        {
            prob.y[i] = node_class_labels[i];
            double[] values = node_values[i];
            int [] indexes = node_indexes[i];
            prob.x[i] = new svm_node[values.length];
            for (int j = 0; j < values.length; j++)
            {
                svm_node node = new svm_node();
                node.index = indexes[j];
                node.value = values[j];
                prob.x[i][j] = node;
            }
        }
        svm_parameter param = new svm_parameter();
        param.probability = 1;
        param.gamma = 0.5;
        param.nu = 0.5;
        param.C = 1;
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.RBF;
        param.cache_size = 20000;
        param.eps = 0.001;
        double[] target = new double[prob.l];
        int total_correct=0;
        svm.svm_cross_validation(prob,param,4,target);
        for(int i=0;i<prob.l;i++)
            if(target[i] == prob.y[i])
                ++total_correct;
        String paramsString="Cross Validation =4\nCross Validation Accuracy = "+100.0*total_correct/prob.l+"%\n"+"Gamma ="+param.gamma+"\nNu ="+param.nu+"\nC ="+param.C+"\nSVM_TYPE ="
                +param.svm_type+"\nKernel_TYPE ="+param.kernel_type+"\nEps ="+param.eps;
        tv2.setText(paramsString);
        Log.d("cva","Cross Validation Accuracy = "+100.0*total_correct/prob.l+"%\n");
         model = svm.svm_train(prob, param);
        return model;
    }

    public String readFromFile(){
        StringBuilder sb = new StringBuilder();
        try{
            final File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "testFileAll.txt");
            FileInputStream fis=new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            Log.d("read", "readExternal: "+sb);

        }catch (Exception e){
            Log.d("read", "readFromFile: exception");
            Toast.makeText(this, "Exception occurred!!", Toast.LENGTH_SHORT).show();
        }
        return sb.toString();
    }

    public void run(View view) {
        flag=0;
        sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_GAME);
    }


    public void trainModel(View view) {
        tv.setText("Reading from file....");
        String data=readFromFile();
        list=data.split(" ");
        tv.setText("Training the model....");
        dope();
        tv.setText("Training finished");
    }
}
