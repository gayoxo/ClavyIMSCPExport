/**
 * 
 */
package fdi.ucm.server.exportparser.html.oda;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


import fdi.ucm.server.modelComplete.ImportExportDataEnum;
import fdi.ucm.server.modelComplete.ImportExportPair;
import fdi.ucm.server.modelComplete.CompleteImportRuntimeException;
import fdi.ucm.server.modelComplete.SaveCollection;
import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.CompleteLogAndUpdates;

/**
 * Clase que impementa el plugin de oda para Localhost
 * @author Joaquin Gayoso-Cabada
 *
 */
public class HTMLSaveCollectionOdA extends SaveCollection {

	private static final String ODA = "HTML de datos en OdA";
	private ArrayList<ImportExportPair> Parametros;
	private ArrayList<Long> ListaDeDocumentos;
	private String Path;
	private String FileIO;
//	private static final int BUFFER = 2048;
	private List<String> fileList; 
	private String OUTPUT_ZIP_FILE = "";
	private String SOURCE_FOLDER = ""; // SourceFolder path
	private ArrayList<Long> DocumentsList;
	private ArrayList<Long> StructureList;
	private boolean Admin;
	private static final Pattern regexAmbito = Pattern.compile("^[0-9]+(,[0-9]+)*$");

	
	/**
	 * Constructor por defecto
	 */
		public HTMLSaveCollectionOdA() {
			DocumentsList=new ArrayList<Long>();
			StructureList=new ArrayList<Long>();
			Admin=true;
	}

	/* (non-Javadoc)
	 * @see fdi.ucm.server.SaveCollection#processCollecccion(fdi.ucm.shared.model.collection.Collection)
	 */
	@Override
	public CompleteLogAndUpdates processCollecccion(CompleteCollection Salvar,
			String PathTemporalFiles) throws CompleteImportRuntimeException{
		try {
			
			CompleteLogAndUpdates CL=new CompleteLogAndUpdates();
			
			if (!ListaDeDocumentos.isEmpty())
			{
			
			Path=PathTemporalFiles;
			SOURCE_FOLDER=Path+"HTML"+File.separator;
			File Dir=new File(SOURCE_FOLDER);
			Dir.mkdirs();
			
			
			
			HTMLprocessOdA oda= new HTMLprocessOdA(ListaDeDocumentos,Salvar,SOURCE_FOLDER,CL,DocumentsList,Admin,StructureList);
			
			
			oda.preocess();
				
			fileList = new ArrayList<String>();
			OUTPUT_ZIP_FILE = Path+System.currentTimeMillis()+".zip";
			FileIO=OUTPUT_ZIP_FILE;
			
			generateFileList(new File(SOURCE_FOLDER));
			try {
				zipIt(OUTPUT_ZIP_FILE);
				CL.getLogLines().add("Descarga el zip");
			} catch (Exception e) {
				e.printStackTrace();
				CL.getLogLines().add("Error en zip, refresca las imagenes manualmente");
			}

			return CL;
			}
			else 
			{
				CL.getLogLines().add("Error en lista, numero de documentos vacio");
				return CL;
			}

		} catch (CompleteImportRuntimeException e) {
			System.err.println("Exception HTML " +e.getGENERIC_ERROR());
			e.printStackTrace();
			throw e;
		}
		
	}

	/**
	 * QUitar caracteres especiales.
	 * @param str texto de entrada.
	 * @return texto sin caracteres especiales.
	 */
	public String RemoveSpecialCharacters(String str) {
		   StringBuilder sb = new StringBuilder();
		   for (int i = 0; i < str.length(); i++) {
			   char c = str.charAt(i);
			   if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_') {
			         sb.append(c);
			      }
		}
		   return sb.toString();
		}

	


	@Override
	public ArrayList<ImportExportPair> getConfiguracion() {
		if (Parametros==null)
		{
			ArrayList<ImportExportPair> ListaCampos=new ArrayList<ImportExportPair>();
			ListaCampos.add(new ImportExportPair(ImportExportDataEnum.Text, "Number de los IDOV en OdA para exportar separados por ','"));
			Parametros=ListaCampos;
			return ListaCampos;
		}
		else return Parametros;
	}

	@Override
	public void setConfiguracion(ArrayList<String> DateEntrada) {
		if (DateEntrada!=null)
		{
			String Entrada=DateEntrada.get(0).trim();
			if (Entrada.endsWith(","))
				Entrada=Entrada.substring(0, Entrada.length()-1);
			
			if (testList(Entrada))
				ListaDeDocumentos=generaListaDocuments(Entrada);
			else
				throw new CompleteImportRuntimeException("La lista de documentos no posee una forma normal, debe de tener la forma: \"####,####,####\"");
			
			

		}
	}
		

	private ArrayList<Long> generaListaDocuments(String string) {
		String[] strings=string.split(",");
		ArrayList<Long> Salida=new ArrayList<Long>();
		for (String string2 : strings) {
			try {
				Long N=Long.parseLong(string2);
				Salida.add(N);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return Salida;
	}

	@Override
	public String getName() {
		return ODA;
	}


	@Override
	public boolean isFileOutput() {
		return true;
	}

	@Override
	public String FileOutput() {
		return FileIO;
	}

	@Override
	public void SetlocalTemporalFolder(String TemporalPath) {
		
	}

	public void zipIt(String zipFile)
	{
	   byte[] buffer = new byte[1024];
	   String source = "";
	   FileOutputStream fos = null;
	   ZipOutputStream zos = null;
	   try
	   {
//	      try
//	      {
//	         source = SOURCE_FOLDER.substring(SOURCE_FOLDER.lastIndexOf("\\") + 1, SOURCE_FOLDER.length());
//	      }
//	     catch (Exception e)
//	     {
//	        source = SOURCE_FOLDER;
//	     }
	     fos = new FileOutputStream(zipFile);
	     zos = new ZipOutputStream(fos);

	     System.out.println("Output to Zip : " + zipFile);
	     FileInputStream in = null;

	     for (String file : this.fileList)
	     {
	        System.out.println("File Added : " + file);
	        ZipEntry ze = new ZipEntry(source + File.separator + file);
	        zos.putNextEntry(ze);
	        try
	        {
	           in = new FileInputStream(SOURCE_FOLDER + File.separator + file);
	           int len;
	           while ((len = in.read(buffer)) > 0)
	           {
	              zos.write(buffer, 0, len);
	           }
	        }
	        finally
	        {
	           in.close();
	        }
	     }

	     zos.closeEntry();
	     System.out.println("Folder successfully compressed");

	  }
	  catch (IOException ex)
	  {
	     ex.printStackTrace();
	  }
	  finally
	  {
	     try
	     {
	        zos.close();
	     }
	     catch (IOException e)
	     {
	        e.printStackTrace();
	     }
	  }
	}

	public void generateFileList(File node)
	{

	  // add file only
	  if (node.isFile())
	  {
	     fileList.add(generateZipEntry(node.toString()));

	  }

	  if (node.isDirectory())
	  {
	     String[] subNote = node.list();
	     for (String filename : subNote)
	     {
	        generateFileList(new File(node, filename));
	     }
	  }
	}

	private String generateZipEntry(String file)
	{
	   return file.substring(SOURCE_FOLDER.length()-1, file.length());
	}
 
	
	public static void main(String[] args) {
		System.out.println(testList("6658,6658,6658,asd"));
		System.out.println(testList("6658"));
	}

	private static boolean testList(String number) {
		if (number==null||number.isEmpty())
			return true;
		 Matcher matcher = regexAmbito.matcher(number);
		return matcher.matches();
	}

	/**
	 * @return the documentsList
	 */
	public ArrayList<Long> getDocumentsList() {
		return DocumentsList;
	}

	/**
	 * @param documentsList the documentsList to set
	 */
	public void setDocumentsList(ArrayList<Long> documentsList) {
		DocumentsList = documentsList;
	}

	/**
	 * @return the structureList
	 */
	public ArrayList<Long> getStructureList() {
		return StructureList;
	}

	/**
	 * @param structureList the structureList to set
	 */
	public void setStructureList(ArrayList<Long> structureList) {
		StructureList = structureList;
	}

	/**
	 * @return the admin
	 */
	public boolean isAdmin() {
		return Admin;
	}

	/**
	 * @param admin the admin to set
	 */
	public void setAdmin(boolean admin) {
		Admin = admin;
	}
	
	
	
}
