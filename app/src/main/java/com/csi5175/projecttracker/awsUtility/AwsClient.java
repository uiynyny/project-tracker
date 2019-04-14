package com.csi5175.projecttracker.awsUtility;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

public class AwsClient {
    private Context appContext;
    //    private static AwsClient awsClientInstance = null;
    private static final String TAG = AwsClient.class.getSimpleName();


    public AwsClient(Context context) {
        appContext = context;
        AWSMobileClient.getInstance().initialize(appContext, new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                Log.i(TAG, "AWSMobileClient initialized. User State is " + result.getUserState());
            }
            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Initialization error.", e);
            }
        });
    }

    public AmazonS3Client getS3Client() {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                appContext,
                AwsConstant.IdentityPoolId.getContent(), // Identity pool ID
                Regions.US_EAST_1 // Region
        );
        return new AmazonS3Client(credentialsProvider, Region.getRegion(Regions.US_EAST_1));
    }
}
