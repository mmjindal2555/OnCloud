package com.iem.manish.oncloud;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;

/**
 * Created by Manish on 1/19/2016.
 */
public class ProfileCredentialsProvider extends Object implements AWSCredentials {

    @Override
    public String getAWSAccessKeyId() {
        return "AKIAJ5OV53T44TQOPXOQ";
    }

    @Override
    public String getAWSSecretKey() {
        return "8hPE1NL/SQf/STYHweRY4apB+Tu1/d7yRvWVGX0+";
    }
}
