package com.csi5175.projecttracker.awsUtility;

public enum AwsConstant {
    IdentityPoolId("us-east-1:f72e8264-86b5-4aa7-8834-391620793c8a"),
    bucket("projecttracker5716c4885b4540da8cd2a130f278820f-tracker"),
    dir("public/project"),
    prefix("project"), info("Name: Yan Zhang\nEmail: yzhan648@uottawa.ca");

    private final String content;

    AwsConstant(String s) {
        this.content = s;
    }

    public String getContent() {
        return content;
    }
}
