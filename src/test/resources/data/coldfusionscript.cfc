<head>
<title>CFScript Example</title>
</head>
<body>
<cfscript>

// Set the variables

acceptedApplicants[1] = "Cora Cardozo";
acceptedApplicants[2] = "Betty Bethone";
acceptedApplicants[3] = "Albert Albertson";
rejectedApplicants[1] = "Erma Erp";
rejectedApplicants[2] = "David Dalhousie";
rejectedApplicants[3] = "Franny Farkle";
applicants.accepted=acceptedApplicants;
applicants.rejected=rejectedApplicants;

rejectCode=StructNew();
rejectCode["David Dalhousie"] = "score";
rejectCode["Franny Farkle"] = "too late";

//Sort and display accepted applicants

ArraySort(applicants.accepted,"text","asc");
WriteOutput("The following applicants were accepted:<hr>");
for (j=1;j lte ArrayLen(applicants.accepted);j=j+1) {
    WriteOutput(applicants.accepted[j] & "<br>");
}
WriteOutput("<br>");

/*
 * sort and display rejected applicants with reasons information
 */

ArraySort(applicants.rejected,"text","asc");
WriteOutput("The following applicants were rejected:<hr>");
for (j=1;j lte ArrayLen(applicants.rejected);j=j+1) {
    applicant=applicants.rejected[j];
    WriteOutput(applicant & "<br>");
    if (StructKeyExists(rejectCode,applicant)) {
        switch(rejectCode[applicant]) {
            case "score":
                WriteOutput("Reject reason: Score was too low.<br>");
                break;
            case "late":
                WriteOutput("Reject reason: Application was late.<br>");
                break;
            default:
                WriteOutput("Rejected with invalid reason code.<br>");
        } //end switch
    } //end if
    else {
        WriteOutput("Reject reason was not defined.<br>");
    } //end else
    WriteOutput("<br>");
} //end for
</cfscript>
