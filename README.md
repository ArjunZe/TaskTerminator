Use Case: Find a long running jobs and stop it

Solution:

This java based tool can monitor Informatica Cloud jobs and kill them if run time exceeds configured time

1.Download the zip, extract the jar and xml file from github release
2.Config the info.xml
3.Run " java -jar TaskTerminator.jar " (without quotes)

Info.xml

host – Informatica Cloud URL.
user – Informatica Cloud User
pass – Informatica Cloud Password
pollInterval - sleep time for the tool before it poll Monitor API. Ex: if set to 2, every two minutes the job status is checked.
maxWait – job execution. Ex: if set to 10, any job that runs more than 10 mins will be killed.
DST – DST change cause 1 hr problem. Set 1 if DST is in Effect or 0
