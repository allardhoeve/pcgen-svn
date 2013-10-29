<#-- 
# Freemarker template for the PCGen variable report.
# Copyright James Dempsey, 2013
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
#
# Created on 2013-04-13 08:30:00 AM
#
# $Id: $
-->
<#macro outputVarOrNbsp strVar>
	<#if strVar?trim?length == 0>
		&nbsp;
	<#else>
		${strVar}
	</#if>
</#macro>  
		
	

<#escape x as x?html>
<html>
<head>
<title>Demonstration PCGen Freemarker Report - ${.now?date}</title>
<style>
table.summary  {border: 1px solid #bbb; font-family: Verdana,Helvetica,sans serif;
font-size: 11px;}
.summary th {font-weight: bold; background-color: rgb(240, 240, 240);}
.summary td {border-bottom: 1px solid #bbb; }
.lastRow td {border-bottom: none; }
td.right {text-align: right;}
table.variable  {border: 1px solid #bbb; font-family: Verdana,Helvetica,sans serif;
font-size: 11px; width:99%;}
.variable th {font-weight: bold; background-color: rgb(240, 240, 240);}
.variable td {border-top: 1px solid #bbb; }
</style>
</head>
<body>
<!-- Produced on ${.now?date} at ${.now?time} using template ${.template_name} -->
<h1>Demonstration PCGen Freemarker Report - ${.now?date}</h1>

<p> Name: <@pcstring tag="NAME"/> </p>

<p> Fighter: ${pcvar("CL=Fighter")} </p>
<p> Rogue: ${pcvar("CL=Rogue")} </p>
<p><b>End of report.</b></p>
</body>
</html>
</#escape>