# S3InputPlugin Plugin for Graylog

[![Build Status](https://travis-ci.org/sherzberg/graylog-plugin-s3.svg?branch=master)](https://travis-ci.org/sherzberg/graylog-plugin-s3)

This plugin provides an input plugin for AWS S3 files. It reads [S3 Events via SQS](http://docs.aws.amazon.com/AmazonS3/latest/dev/NotificationHowTo.html) from your AWS account to then pull in the S3 files into Graylog for processing.

**Required Graylog version:** See below version table:

|graylog version|s3 plugin version|
|---|---|
|< 2.3.0|< 2.3.0|
|2.3.x|2.3.x|

## Installation

[Download the plugin](https://github.com/https://github.com/sherzberg/graylog-plugin-s3/releases)
and place the `.jar` file in your Graylog plugin directory. The plugin directory
is the `plugins/` folder relative from your `graylog-server` directory by default
and can be configured in your `graylog.conf` file.

Restart `graylog-server` and you are done.

## S3 setup and configuration

### Step 1: Write some logs to S3

Start by enabling something to write log files to S3, like FluentD (http://docs.fluentd.org/articles/out_s3):

The writer could be unique to your setup, so this is left to the reader. The log line contents can be raw text or json. No processing or extraction is done by this plugin. It is left up to you.

### Step 2: Enable S3 Event Notifications to SNS

Create an SNS topic to write the events to, then configure S3 Events for Create and Delete to write to the SNS topic created above (remember the name of the SNS topic for later).

### Step 3: Set up SQS for S3 notifications

Navigate to the AWS SQS service (in the same region as the SNS topic) and hit **Create New Queue**.

You can leave all settings on their default values for now but write down the **Queue Name** because you will need it for the Graylog configuration later. Our recommended default value is *s3-notifications*.

S3 will write notifications about log files that were created or removed. Let’s subscribe the SQS queue to the S3 SNS topic you created in the first step now:

Right click on the new queue you just created and select *Subscribe Queue to SNS Topic*. Select the SNS topic that you configured when setting up your S3 bucket events. **Hit subscribe and you are all done with the AWS configuration.**

### Step 4: Install and configure the Graylog S3 plugin

Copy the `.jar` file that you received to your Graylog plugin directory which is configured in your `graylog.conf` configuration file using the `plugin_dir` variable.

Restart `graylog-server` and you should see the new input type *AWS S3 Input* at *System -> Inputs -> Launch new input*. The required input configuration should be self-explanatory.

**Important:** The IAM user you configured in “System -> Configurations” has to have permissions to read S3 objects and delete and read notifications from SQS:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "Stmt1411854479000",
      "Effect": "Allow",
      "Action": [
        "s3:GetObject"
      ],
      "Resource": [
        "arn:aws:s3:::s3-logfiles/*"
      ]
    }
  ]
}
```

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "Stmt1411834838000",
      "Effect": "Allow",
      "Action": [
        "sqs:DeleteMessage",
        "sqs:ReceiveMessage"
      ],
      "Resource": [
        "arn:aws:sqs:eu-west-1:450000000000:s3-write"
      ]
    }
  ]
}
```

(Make sure to replace *resource* values with the actual ARNs of your environment)

### Usage

You should see your log messages coming in after launching the input. (Note that it can take a few minutes based on how frequent systems are accessing your AWS resource) **You can even stop Graylog and it will catch up with all S3 log data that have been written since it was stopped when it is started again.**

**Now do a search in Graylog. Select “Search in all messages” and search for:** `source:"s3"`

The raw log lines in your S3 objects should show up in your search results. You can now use Extractors, Plugins, Pipeline, etc to process your raw messages.

For example, if you write json lines to your S3 files, you can use the JSON Extractor to extract all the json attributes.


## Development

This project is using Maven 3 and requires Java 8 or higher.

* Clone this repository.
* Run `mvn package` to build a JAR file.
* Optional: Run `mvn jdeb:jdeb` and `mvn rpm:rpm` to create a DEB and RPM package respectively.
* Copy generated JAR file in target directory to your Graylog plugin directory.
* Restart the Graylog.

## Plugin Release


We are using the maven release plugin:

```
$ mvn release:prepare
[...]
$ mvn release:perform
```

This sets the version numbers, creates a tag and pushes to GitHub. Travis CI will build the release artifacts and upload to GitHub automatically.
