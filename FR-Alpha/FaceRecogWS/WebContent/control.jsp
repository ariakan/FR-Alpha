<%@ page language="java" import="java.sql.*, jrc.ipsc.surcit.FileUtils"%>
<html>
<head><title>FaceDB Control Page</title>
</head>
<body>

<p align="center"><b>Following records are selected from the 'namefaces' table.</b><br>&nbsp;</p>

<div align="center" width="85%">
<center>
<table border="1" borderColor="#ffe9bf" cellPadding="0" cellSpacing="0" width="658" height="63">
<tbody>
<td bgColor="#008080" width="47" align="center" height="19"><font color="#ffffff"><b>No.</b></font></td>
<td bgColor="#008080" width="107" height="19"><font color="#ffffff"><b>Name</b></font></td>
<td bgColor="#008080" width="224" height="19"><font color="#ffffff"><b>Surname</b></font></td>
<td bgColor="#008080" width="270" height="19"><font color="#ffffff"><b>id</b></font></td>
<td bgColor="#008080" width="270" height="19"><font color="#ffffff"><b>face</b></font></td>

<%
String trainingPath = FileUtils.getTrainingPath();
System.out.println(trainingPath);
String DRIVER = "org.gjt.mm.mysql.Driver";
Class.forName(DRIVER).newInstance();


Connection con=null;
ResultSet rst=null;
Statement stmt=null;
String infourl = "info.jsp?id=";
try{
String url="jdbc:mysql://localhost:3306/faceinfo?user=faceinfo&password=faceinfo";


int i=1;
con=DriverManager.getConnection(url);
stmt=con.createStatement();
rst=stmt.executeQuery("SELECT name, surname, personid, jpegname FROM `namefaces` GROUP BY personid");
while(rst.next()){

if (i==(i/2)*2){
%>
<tr>
<td bgColor="#ffff98" vAlign="top" width="47" align="center" height="19"><%=i%>.</td>
<td bgColor="#ffff98" vAlign="top" width="107" height="19"><%=rst.getString(1)%></td>
<td bgColor="#ffff98" vAlign="top" width="224" height="19"><%=rst.getString(2)%></td>
<td bgColor="#ffff98" vAlign="top" width="270" height="19"><%=rst.getString(3)%></td>
<td bgColor="#ffff98" vAlign="top" width="270" height="19"><a href="<%=infourl+rst.getString(3)%>"><img src="<%=trainingPath+"\\"+rst.getString(4)%>" /></a></td>
</tr>
<%
}else{
%>
<tr>
<td bgColor="#ffcc68" vAlign="top" width="47" align="center" height="19"><%=i%>.</td>
<td bgColor="#ffcc68" vAlign="top" width="107" height="19"><%=rst.getString(1)%></td>
<td bgColor="#ffff98" vAlign="top" width="224" height="19"><%=rst.getString(2)%></td>
<td bgColor="#ffcc68" vAlign="top" width="270" height="19"><%=rst.getString(3)%></td>
<td bgColor="#ffff98" vAlign="top" width="270" height="19"><a href="<%=infourl+rst.getString(3)%>"><img src="<%=trainingPath+"\\"+rst.getString(4)%>"  /></a></td>
</tr>
<%	}

i++;
}
rst.close();
stmt.close();
con.close();
}catch(Exception e){
System.out.println(e.getMessage());
}
%>

</tbody>
</table>
</center>
</div>


</body>
</html>