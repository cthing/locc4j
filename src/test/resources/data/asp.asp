<%@LANGUAGE="JScript"%>
<!-- #include file="include/UtilsInc.asp" -->
<!-- #include file="include/HeadInc.asp" -->
<!-- #include file="include/BodyInc.asp" -->

<%
CurrentPage = HOME_PAGE;


function LinkFunc()
{
%>
<%
}

function ContentFunc()
{ %>
<div class="HomeContent">
	<span class="HomeHilite">Lorem ipsum dolor sit amet -</span> Lorem ipsum dolor sit amet, consectetur
	 adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim
	 veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute
	 irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur
	 sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.<br><br>
</div>
<div class="HomeContent">
	<span class="HomeHilite">Lorem ipsum dolor -</span> Lorem ipsum dolor sit amet, consectetur
    adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
	<a href="https://github.com/foobar">tempor incididunt</a> of the
	<a href="https://github.com/barfoo">tempor incididunt</a>.
	Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et
	dolore magna aliqua. <b>Lorem ipsum dolor sit amet</b>.<br><br>
</div>
<table cellspacing="0" cellpadding="0" border="0">
	<tr>
		<td width="45%" valign="top">
			<div class="HomeContent">
				<span class="HomeHilite">Lorem ipsum dolor sit amet, consectetur
                adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim
                veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute
                irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur
                sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
			</div>
		</td>
		<td width="10%"></td>
		<td width="45%" valign="top">
			<div class="HomeContent">
				<a href="Downloads.asp" title="Downloads"></a>
				<span class="HomeHilite">Software -</span>
				sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
			</div>
		</td>
	</tr>
</table>
<%
}
%>


<!doctype html public "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
<%		ShowHead("Foo Bar Baz",
			  	"occaecat cupidatat non proident, sunt in culpa.",
			  	"non proident, sunt in culpa qui officia deserunt");
%>
	</head>

	<body marginheight="0" marginwidth="0">
<%		ShowBody("non proident, sunt in culpa qui officia deserunt", LinkFunc, ContentFunc); %>
	</body>
</html>
