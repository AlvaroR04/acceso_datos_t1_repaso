import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;
import java.util.Scanner;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

public class Main {
    public static void main(String[] args) {
        try {
            int bytesTamChar = 2; //tamanio en bytes del writeChars
            int longitudNombre = 15;

            int longitudRegistro = 4 + (longitudNombre * bytesTamChar) +
                    4 + 1;

            int[] numsProtones = {1, 26, 80, 118};
            String[] nombres = {"Hidrógeno", "Hierro", "Mercurio", "Oganesón"};
            float[] masas = {1.0078f, 55.845f, 200.59f, 294f};
            boolean[] sonMetales = {false, true, true, false};

            int numProtones = 0;
            char[] nombre = new char[longitudNombre];
            float masa = 0f;
            boolean esMetal = false;

            File ficheroLog = new File("atomos.txt");
            File ficheroBinario = new File("atomos.dat");
            File ficheroXml = new File("atomos.xml");
            File ficheroXsl = new File("atomos.xsl");
            File ficheroHtml = new File("atomos.html");

            if(!ficheroBinario.exists()) {
                RandomAccessFile file = new RandomAccessFile(ficheroBinario, "rw");
                int posicion = 0;

                //--- ESCRIBIR FICHERO BINARIO ---

                for(int i = 0; i < numsProtones.length; i++) {
                    posicion = (numsProtones[i] - 1) * longitudRegistro;
                    file.seek(posicion);

                    file.writeInt(numsProtones[i]);

                    StringBuffer strBuf = new StringBuffer(nombres[i]);
                    strBuf.setLength(longitudNombre);
                    file.writeChars(strBuf.toString());

                    file.writeFloat(masas[i]);

                    file.writeBoolean(sonMetales[i]);
                }
                file.close();
                posicion = 0;

                int numAtomico = 1;

                RandomAccessFile fileAbrir = new RandomAccessFile(ficheroBinario, "rw");

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                DOMImplementation implementation = builder.getDOMImplementation();

                Document document = implementation.createDocument(null, "Atomos", null);
                document.setXmlVersion("1.0");

                FileWriter escritor = new FileWriter(ficheroLog, true);
                String res = "";

                //--- LEER FICHERO BINARIO Y ESCRIBIR XML ---

                while(posicion < fileAbrir.length()) {
                    fileAbrir.seek(posicion);

                    numProtones = fileAbrir.readInt();

                    for(int i = 0; i < longitudNombre; i++) {
                        nombre[i] = fileAbrir.readChar();
                    }
                    String tmpNombre = new String(nombre);

                    masa = fileAbrir.readFloat();

                    esMetal = fileAbrir.readBoolean();

                    if(numProtones > 0) {
                        Element raiz = document.createElement("atomo");
                        document.getDocumentElement().appendChild(raiz);

                        crearElemento("protones", Integer.toString(numProtones), raiz, document);
                        crearElemento("nombre", tmpNombre.trim(), raiz, document);
                        crearElemento("masa", Float.toString(masa), raiz, document);
                        crearElemento("metal", Boolean.toString(esMetal), raiz, document);
                    }

                    res += "[Átomo nº " + numProtones + "]\n" +
                                "\tNombre: " + tmpNombre.trim() + "\n" +
                                "\tMasa: " + masa + "\n" +
                                "\t¿Es un metal? " + esMetal + "\n\n";

                    escritor.write(res);

                    res = "";
                    
                    numAtomico++;
                    posicion = (numAtomico - 1) * longitudRegistro;
                }
                escritor.close();
                fileAbrir.close();

                Source source = new DOMSource(document);
                Result result = new StreamResult(ficheroXml);

                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.transform(source, result);

                //--- LEER XML CON SAX ---

                XMLReader procesadorXML = XMLReaderFactory.createXMLReader();
                GestionContenido gestor = new GestionContenido();
                procesadorXML.setContentHandler(gestor);

                InputSource ficheroXML = new InputSource(ficheroXml.getName());
                procesadorXML.parse(ficheroXML);

                //--- CONVERSION A HTML ---

                FileOutputStream fOs = new FileOutputStream(ficheroHtml);

                Source estilos = new StreamSource(ficheroXsl);
                Source datos = new StreamSource(ficheroXml);
                Result resultado = new StreamResult(fOs);

                Transformer transformador = TransformerFactory.newInstance().newTransformer(estilos);
                transformador.transform(datos, resultado);

                fOs.close();

                //--- LEER LOG ---

                Scanner lector = new Scanner(ficheroLog);

                while(lector.hasNextLine()) {
                    System.out.println(lector.nextLine());
                }
            } else {
                System.err.println("El fichero binario ya existe");
            }
        } catch(IOException ioEx) {
//            System.err.println("Error de E/S");
            ioEx.printStackTrace();
        } catch (ParserConfigurationException e) {
            System.err.println("Error con el parser XML");
        } catch (TransformerConfigurationException e) {
            System.err.println("Error en la configuración del transformador");
        } catch (TransformerException e) {
            System.err.println("Error en el transformador");
        } catch (SAXException e) {
            System.err.println("Error con el SAX");
        }
    }

    private static void crearElemento(String datoEmple, String valor, Element raiz, Document document) {
        Element elem = document.createElement(datoEmple);
        Text texto = document.createTextNode(valor);
        raiz.appendChild(elem);
        elem.appendChild(texto);
    }
}

class GestionContenido extends DefaultHandler {
    public GestionContenido() {
        super();
    }

    public void startDocument() {
        System.out.println("Comienzo del documento XML");
    }

    public void endDocument() {
        System.out.println("Final del documento XML");
    }

    public void startElement(String uri, String nombre, String nombreC, Attributes atts) {
        System.out.println("\tElemento del principio: " + nombre);
    }

    public void endElement(String uri, String nombre, String nombreC) {
        System.out.println("\tElemento del final: " + nombre);
    }

    public void characters(char[] ch, int inicio, int longitud) throws SAXException {
        String car = new String(ch, inicio, longitud);
        car.replaceAll("[\t\n]", "");
        System.out.println("\tCaracteres: " + car);
    }
}
