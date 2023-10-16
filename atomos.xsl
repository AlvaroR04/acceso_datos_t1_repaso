<?xml version="1.0" encoding='ISO-8859-1'?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 <xsl:template match='/'>
   <html><xsl:apply-templates /></html>
 </xsl:template>
 <xsl:template match='Atomos'>
    <head><title>LISTADO DE ATOMOS</title></head>
    <body> 
    <h1>LISTA DE ATOMOS</h1>
    <table border='1'>
    <tr><th>Protones</th><th>Nombre</th><th>Masa</th><th>Metal</th></tr>
      <xsl:apply-templates select='atomo' />
    </table>
    </body>
 </xsl:template>
 <xsl:template match='atomo'>
   <tr><xsl:apply-templates /></tr>
 </xsl:template>
 <xsl:template match='protones|nombre|masa|metal'>
   <td><xsl:apply-templates /></td>
 </xsl:template>
</xsl:stylesheet>