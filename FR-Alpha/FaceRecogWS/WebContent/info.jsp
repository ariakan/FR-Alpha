
<%@ page language="java" import="java.sql.*, jrc.ipsc.surcit.FileUtils"%>

<%
String DRIVER = "org.gjt.mm.mysql.Driver";
Class.forName(DRIVER).newInstance();


Connection con=null;
ResultSet rst=null;
Statement stmt=null;

if (request.getParameter("id") == null) {
    out.println("Please enter your name.");
} else {

    

try{
String url="jdbc:mysql://localhost:3306/faceinfo?user=faceinfo&password=faceinfo";
String imgpath = FileUtils.getTrainingPath();;
int i=1;
con=DriverManager.getConnection(url);
stmt=con.createStatement();
String query = "SELECT name, surname, personid, jpegname, date FROM `namefaces` WHERE personid = '"+request.getParameter("id")+"'";

rst=stmt.executeQuery(query);



%>
<html>
<head><title>id Control Page</title>
</head>
<body>


<%
while(rst.next())
{
if (i == 1){
	System.out.println(rst.getString(1));


%>
<p align="center"><b>Following records are selected for <%=rst.getString(1)%> <%=rst.getString(2)%> .</b><br>&nbsp;</p>

<div align="center" width="85%">
<center>
<table border="1" borderColor="#ffe9bf" cellPadding="0" cellSpacing="0" width="658" height="63">
<tbody>
<td bgColor="#008080"  align="center" height="19"><font color="#ffffff"><b>Pic</b></font></td>
<td bgColor="#008080"  height="19"><font color="#ffffff"><b>Date</b></font></td>
<tr>
<td align="right"><img src="<%=imgpath+"\\"+rst.getString(4)%>" /></td>
<td align="center"><%=rst.getString(5)%></td>
</tr>
<%}else{ %>

<tr>
<td align="right"><img src="<%=imgpath+"\\"+rst.getString(4)%>" /></td>
<td align="center"><%=rst.getString(5)%></td>
</tr>
<% 
}
i++;
}%>
</tbody>
</table>
</center>
</div>


</body>
</html>
<%
rst.close();
stmt.close();
con.close();
}
catch(Exception e){
System.out.println(e.getMessage());
}
}
%>

