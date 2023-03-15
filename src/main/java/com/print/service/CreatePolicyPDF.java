package com.print.service;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.Barcode;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.TextField;
import com.itextpdf.text.pdf.qrcode.ErrorCorrectionLevel;

@Component
public class CreatePolicyPDF {

	@Value("${value.pathfile.output}")
	private String pathfileout;
	//@Value("${value.pathfile.template}")
	//private String templete;
	@Value("${value.font}")
	private String fontbase;	
	
	
	public byte[] setFilePDF(JSONObject jsonObject) {	 

		try {
		

			//JSONObject path = (JSONObject) jsonObject.get("path");			
			JSONArray schedule = (JSONArray)jsonObject.get("schedule");
			JSONObject detail = (JSONObject) jsonObject.get("detail");
			
			
			//String namefile = path.getString("namefile");		
			//String fileout = pathfileout + namefile;	
		

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			Document document = new Document();	
			document.setPageSize(PageSize.A4);
			PdfCopy copy = new PdfCopy(document, bos);
			document.open();   

			//Map<String, String> scheduleMap  =  mapDataArray(schedule);

			Map<String, String> detailMap  = mapDataObject(detail);

			setForm(copy,schedule,detailMap);
			
		    //scheduleMap.clear();
			document.close();
		    copy.close();
		    byte[] setData = bos.toByteArray();
		    //bos.close();
		    //FileOutputStream fileOutputStream = new FileOutputStream(fileout);
	    	//bos.writeTo(fileOutputStream);
	    	//bos.close();
	    	//fileOutputStream.close();
	    	
		    //AddCertificate addcer = new  AddCertificate();
		    //bos = addcer.AddCert(bos);
//		    if(jdata.getString("scheduleSet").equals("YES")) {
//
//		    	File filePDF = new File(fileout);
//				if(filePDF.getAbsoluteFile().exists()) { 
//				    mergePdfs(bos,fileout,pathfile,namefile);
//				    
//					bos.close();					
//				}else {
//					
//					FileOutputStream fileOutputStream = new FileOutputStream(fileout);					
//			    	bos.writeTo(fileOutputStream);
//			    	bos.close();
//			    	fileOutputStream.close();
//				}
//		    }else {
//		    	
//		    	FileOutputStream fileOutputStream = new FileOutputStream(fileout);
//		    	bos.writeTo(fileOutputStream);
//		    	bos.close();
//		    	fileOutputStream.close();
//		    }

		    //System.out.println("AddTextOnPDF Complete");	    
		    return setData;
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;		
		
	}
	
	protected void  setForm(PdfCopy copy, JSONArray scheduleMap, Map<String, String> detailMap) throws FileNotFoundException, IOException, DocumentException, JSONException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();		
		for(int i = 0; i < scheduleMap.length() ; i++) {
			JSONObject setDetail = new JSONObject();
    		setDetail = (JSONObject)scheduleMap.get(i);
    		
    		String templete = "D:\\WebServiceToWebService\\motor\\"+setDetail.get("formPage").toString()+".pdf";
    		File ftmp = new File(templete);
    		if(ftmp.exists() && !ftmp.isDirectory()) { 
	    		   
	    		
		    	PdfReader reader = new PdfReader(new FileInputStream(templete));
		
		    	PdfStamper stamper = new PdfStamper(reader, baos);
		    	
			    AcroFields fields = stamper.getAcroFields();		        	
				fields.setGenerateAppearances(true);
		        stamper.setFormFlattening(true);
		            
				setNameField(fields, stamper, detailMap);	
				Object[] keys = fields.getFields().keySet().toArray();
				for(int f = 0;f < keys.length;f++) {
					fields.removeField((String)keys[f]);
				}
			    stamper.close();		  
		    	
		    	int copyPage =  Integer.parseInt(setDetail.get("pageCopy").toString());
		    	
		    	for(int j = 0; j < copyPage ; j++) {	    		  		
		    		 
			    	reader = new PdfReader(baos.toByteArray());
			   	 	copy.addPage(copy.getImportedPage(reader,1)); // Choose page 
			   	 	copy.freeReader(reader);
			   	 	reader.close();        
		        }     
	        }	   
		}
    	baos.close();      	
     }
	
	protected void setNameField(AcroFields fields, PdfStamper stamper, Map<String, String> data) throws IOException, DocumentException {
        // Set font size.
		
		final BaseFont font = BaseFont.createFont(fontbase, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
		
		Map<String, AcroFields.Item> map = new HashMap<String, AcroFields.Item>();
		map = fields.getFields();
		Iterator<String> iterator = map.keySet().iterator();
		while(iterator.hasNext()) {
			String fieldName = iterator.next();
//			String fieldValue = fields.getField(fieldName);
//			int fieldType = fields.getFieldType(fieldName);	
//			System.out.println(fieldName + "(" + fieldType + ") = " + fieldValue );
//			fields.setField(fieldName,fieldName);
			
			
			//System.out.println(fieldType);
			for (Entry<String, String> dataValue : data.entrySet()) {	    		

				if (dataValue.getKey().equals(fieldName)) {
					String[] sentences = fieldName.split("\\_");
					
					if(sentences[0].equals("img")) {
						//System.out.println(sentences[0]);
						if(sentences[1].equals("head"))setImgField(fields, stamper, fieldName,data.get(fieldName));
						else if(sentences[1].equals("barcode"))setBarcode(fields,stamper ,fieldName,data.get(fieldName)); //"barcode"
						else if(sentences[1].equals("qrcode"))setQRCode(fields, stamper, fieldName,data.get(fieldName)); // qrcode
					}else if (sentences[0].equals("txt")) {
						
//
//						PdfDictionary widgetDict = fields.getFieldItem(fieldName).getMerged(0);
//						PdfNumber alignment = widgetDict.getAsNumber(PdfName.FF);
//						System.out.println(fieldName);  
//						System.out.println(alignment); 
//	
//						
//						fields.setFieldProperty(fieldName, "bgcolor", Color.TRANSLUCENT, null);
//		    	    	//fields.setFieldProperty(fieldName, "bordercolor", Color.TRANSLUCENT, null);
//						fields.setFieldProperty(fieldName, "textfont", font, null);    	
//		    	    	//fields.setFieldProperty(fieldName, "textsize", 12f, null);
//		    	    	fields.setFieldProperty(fieldName, "fflags", PdfFormField.FLAGS_INVISIBLE, null);	

						
						PdfDictionary widgetDict = fields.getFieldItem(fieldName).getWidget(0);
						TextField textField = new TextField(null, null, null);
						fields.decodeGenericDictionary(widgetDict, textField);
						float fontsize = textField.getFontSize();  // Font Size
						int txtalign = textField.getAlignment();   // Align Text
						BaseColor bcolor = textField.getTextColor();
						//font = textField.getFont(); // Font Base
						Rectangle rect = fields.getFieldPositions(fieldName).get(0).position;		    	    
						//System.out.println(textField.getFont());
						PdfContentByte over = stamper.getOverContent(1);
						over.beginText();
						over.setFontAndSize(font,fontsize);// set font and size
						over.setColorFill(bcolor);// set color text
						
						String test = "";
						if(data.get(fieldName).equals(""))test = fieldName;
						else test =  data.get(fieldName);
						if(txtalign==0) {						
							over.showTextAligned(PdfContentByte.ALIGN_LEFT, test, rect.getLeft(), rect.getBottom(), 0);	
						}else if(txtalign==2) {	
							over.showTextAligned(PdfContentByte.ALIGN_RIGHT, test, rect.getRight(), rect.getBottom(), 0);				
						}else if(txtalign==1) {	
							over.showTextAligned(PdfContentByte.ALIGN_CENTER, test, rect.getLeft()+((rect.getWidth()/2)), rect.getBottom(), 0);			
						}					
						over.endText();	
						
					}

				} 	
							
			}
			
		}
     }
	
	
	protected void  setImgField(AcroFields fields, PdfStamper stamper ,String field,String value) {	
	
		
    	try {    		
    		
    		Rectangle rect = fields.getFieldPositions(field).get(0).position;
    	    float left   = rect.getLeft();
    	    float width  = rect.getWidth();
    	    float height = rect.getHeight();
    		
    		Image img = Image.getInstance(value);  		
    		img.scaleAbsolute(width,height);

    		
    		img.setAbsolutePosition(left, rect.getBottom());
    		PdfContentByte canvas = stamper.getOverContent(1);
    		canvas.addImage(img);    		
    		
		} catch (BadElementException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	

     }
		
	    
	    protected void setBarcode(AcroFields fields,PdfStamper stamper, String field,String value ) {  

	        Barcode128  barcode = new Barcode128();
	        //barcode.setBaseline(-1); //text to top
	        //final BaseFont font = BaseFont.createFont(fontbase1, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
	       // barcode.setFont(font);  //null removes the printed text under the barcode
	        barcode.setBarHeight(12f); // great! but what about width???
	        //barcode.setSize(9f);
	        barcode.setX(1f); 
	        //barcode.setFont(null);
	        barcode.setCodeType(Barcode.CODE128);
	        barcode.setCode(value);     

		    try {
		    	Rectangle rect = fields.getFieldPositions(field).get(0).position;
	    	    float left   = rect.getLeft();
	    	    float width  = rect.getWidth();
	    	    float height = rect.getHeight();
	    	    Image img = Image.getInstance(barcode.createAwtImage(Color.BLACK, Color.WHITE), null, true);
	    		 		
	    		img.scaleAbsolute(width,height);

	    		
	    		img.setAbsolutePosition(left, rect.getBottom());
	    		PdfContentByte canvas = stamper.getOverContent(1);
	    		canvas.addImage(img);    
			} catch (BadElementException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			 
	               
	    }
	    
	    protected void setQRCode(AcroFields fields, PdfStamper stamper, String field,String value ) {  
	    
	    	Map<EncodeHintType, Object> hashMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class); 
	    	hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
	    	hashMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");	
	    	hashMap.put(EncodeHintType.MARGIN, 0); /* default = 4 */

			try {				

				QRCodeWriter barcodeWriter = new QRCodeWriter();
	    	    BitMatrix bitMatrix =  barcodeWriter.encode(value, BarcodeFormat.QR_CODE, 200, 200,hashMap);
	    	   
				int matrixWidth = bitMatrix.getWidth();			
				BufferedImage qrImg  = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
				qrImg.createGraphics();

				Graphics2D graphics = (Graphics2D) qrImg.getGraphics();
				//graphics.setComposite(AlphaComposite.Clear);
				graphics.setColor(Color.WHITE);
				graphics.fillRect(0, 0, matrixWidth, matrixWidth);
				// Paint and save the image using the ByteMatrix
				graphics.setColor(Color.BLACK);

				for (int i = 0; i < matrixWidth; i++) {
					for (int j = 0; j < matrixWidth; j++) {
						if (bitMatrix.get(i, j)) {
							graphics.fillRect(i, j, 1, 1);
						}
					}
				}
				
				//graphics.setComposite(AlphaComposite.SrcOver);
				graphics.drawImage(qrImg, 0, 0, matrixWidth, matrixWidth, null);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ImageIO.write(qrImg, "gif", bos);
				
				Rectangle rect = fields.getFieldPositions(field).get(0).position;
	    	    float left   = rect.getLeft();
	    	    float width  = rect.getWidth();
	    	    float height = rect.getHeight();
	    		
	    		Image img = Image.getInstance(bos.toByteArray());  		
	    		img.scaleAbsolute(width,height);
	    			
	    		
	    		img.setAbsolutePosition(left, rect.getBottom());
	    		PdfContentByte canvas = stamper.getOverContent(1);
	    		canvas.addImage(img);    
	    		bos.close();
				hashMap.clear();

			} catch (BadElementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WriterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	   
	               
	    }
	    
	    protected Map<String, String> mapDataArray(JSONArray map) throws JSONException {  

	    	
	    	Map<String, String> detailMap  =  new HashMap<String, String>() ;
			for (int i = 0; i < map.length(); i++) {				
				 JSONObject data  = (JSONObject)map.get(i);  
				 @SuppressWarnings("unchecked")
				Iterator<String> keysItr = data.keys();
				 String key = keysItr.next();
				 String value = data.getString(key);
				// System.out.println(key + "-"+value);
				detailMap.put(key,value);
			   		    
			}	    	
			return detailMap;
	    }    
	    protected Map<String, String> mapDataObject(JSONObject map) throws JSONException {  

	    	Map<String, String> detailMap  =  new HashMap<String, String>() ;
		    @SuppressWarnings("unchecked")
			Iterator<String> keysItr = map.keys();
		    while(keysItr.hasNext() ) {
		    	String key = keysItr.next();
				String value = map.getString(key);
				detailMap.put(key, value);
		    }			       	
			return detailMap;
	    } 

	    
}
