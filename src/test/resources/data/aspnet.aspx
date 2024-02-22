<%@ Page Language="C#" AutoEventWireup="true" CodeBehind="Default.aspx.cs"
   Inherits="firstexample._Default" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" >

   <!--
        Comment 1
   -->
   <head runat="server">
      <title>
         Untitled Page
      </title>
   </head>

   <body>

      <!-- Comment 2 -->
      <form id="form1" runat="server">
         <div>

            <asp:TextBox ID="TextBox1" runat="server" style="width:224px">
            </asp:TextBox>

            <br />
            <br />

            <asp:Button ID="Button1" runat="server" Text="Enter..." style="width:85px" onclick="Button1_Click" />
            <hr />

            <h3> Results: </h3>
            <span runat="server" id="changed_text" />

         </div>
      </form>

   </body>

</html>
