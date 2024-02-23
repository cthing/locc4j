<!---attempt to log in --->
<cfset cmdCollectionXML = '<CommandCollection class="array">
   <Command>
      <Target>Login</Target>
      <Method>doLogin</Method>
      <Args>
         <userName>webmaster</userName>
         <password>password</password>
      </Args>
   </Command>
</CommandCollection>'>

<cfhttp url="http://my_server/demo/loader.cfm" method="POST">
   <cfhttpparam type="FORMFIELD" name="csModule" value="components/dashboard/dashboard" />
   <cfhttpparam type="FORMFIELD" name="cmdCollectionXML" value="#cmdCollectionXML#" />
</cfhttp>

<!--- check for http errors--->
<cfif cfhttp.StatusCode neq "200 OK">
   <cfoutput>HTTP Error: #cfhttp.StatusCode#</cfoutput>
<cfelse>

   <cfset cmdResult = Server.CommonSpot.MapFactory.deserialize(cfhttp.fileContent)>
   <cfdump var="#cmdResult#" label="Login.doLogin result">

   <!--- check basic cmd result --->
   <cfif cmdResult[1].status.code neq 200>
      <cfoutput>Error</cfoutput>
      <cfdump var="#cmdResult[1].status#" label="Result status">
   <cfelse>
      <!--- capture returned session cookies into a session var, to pass with later requests --->
      <cfset session.csSessionCookies = cmdResult[1].data.SessionCookies>

      <!---check login result --->
      <cfif cmdResult[1].data.LoginResult eq 0>
         <cfoutput>Login failed.</cfoutput>
      <cfelseif cmdResult[1].data.LoginResult eq 2>
         <cfoutput>Login successful, password change required.</cfoutput>
      <cfelse>

         <!--- we're good, continue --->
         <cfset cmdCollectionXML = '<CommandCollection class="array">
         <Command>
            <Target>Users</Target>
            <Method>getProfile</Method>
            <Args>
               <userID>-1</userID>
            </Args>
         </Command>
         </CommandCollection>'>

         <cfhttp url="http://my_server/demo/loader.cfm" method="POST">
            <cfhttpparam type="FORMFIELD" name="csModule" value="components/dashboard/dashboard" />
            <cfhttpparam type="FORMFIELD" name="cmdCollectionXML" value="#cmdCollectionXML#" />
            <cfhttpparam type="COOKIE" name="JSESSIONID" value="#session.csSessionCookies.jSessionID#" />
            <cfhttpparam type="COOKIE" name="CFID" value="#session.csSessionCookies.cfID#" />
            <cfhttpparam type="COOKIE" name="CFTOKEN" value="#session.csSessionCookies.cfToken#" />
         </cfhttp>

         <cfset cmdResult = Server.CommonSpot.MapFactory.deserialize(cfhttp.fileContent)>
         <cfdump var="#cmdResult#" label="Users.getProfile result">
      </cfif>
   </cfif>

</cfif>
