Attribute VB_Name = "Module1"
Public Function SayHello(Name As String)

    ' Create a response string
    Dim Response As String
    Response = "Hello " & Name

    'Return response string
    SayHello = Response

End Function
