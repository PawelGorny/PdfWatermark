package com.pawelgorny.pdfwatermark;

//
//import com.itextpdf.text.*;
//import com.itextpdf.text.pdf.PdfDictionary;
//import com.itextpdf.text.pdf.PdfName;
//import com.itextpdf.text.pdf.PdfReader;
//import com.itextpdf.text.pdf.PdfWriter;
//import com.itextpdf.text.pdf.parser.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import org.junit.Test;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PDFWatermarkServiceTest {


    static PDFWatermarkService pdfWatermarkService = new PdfWatermarkServiceImpl();

    public static void main(String args[])
    {
        try {
            PDFWatermarkServiceTest test = new PDFWatermarkServiceTest();
            test.testServiceWatermark1();
        }catch (Exception e){
            System.err.println(e.getMessage());
        }
    }

    @Test
    public void testServiceWatermark1() throws Exception{

        // PRE TESTING DATA
        String filename = "watermark-test.pdf";
        File inputFile = generatePdfFromText(filename);
        byte[] result = null;
        String line1 = "DRAFT";
        String line2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(new Date());
        String line3 = "user";

        FileInputStream fis = null;
        ByteArrayOutputStream fos = null;
        try{
            fis = new FileInputStream(inputFile);
            fos = new ByteArrayOutputStream();

            PdfWatermarkSettings settings = new PdfWatermarkSettings(true);
            settings.setWatermarkPosition(PdfWatermarkSettings.WatermarkPosition.DIAGONAL);
            settings.setAlign(PdfWatermarkSettings.Align.ALIGN_LEFT);
            settings.setLayer(PdfWatermarkSettings.Layer.UNDER);
            settings.setOpacity(1f);
            List<String> lines = new ArrayList<String>();
            lines.add(line1);
            lines.add(line2);
            lines.add(line3);

            Map<String, String > infos = new HashMap<>(1);
            infos.put("Title", "Watermark test");
            infos.put("Creator","MyAppJunit");
            settings.setInfos(infos);

            // PERFORM TESTS

            pdfWatermarkService.stamp(fis, fos, lines, settings);

        } finally{
            try{
                if(fis != null){
                    fis.close();
                }
                if(fos != null){
                    fos.close();
                }
            }catch(Exception e){}
        }

        result = fos.toByteArray();


        // ASSERTIONS
        assertTrue(result != null);
        assertTrue(result.length > 0);

//        assertTrue(containsText(new ByteArrayInputStream(result), line1, true));
//        assertFalse(containsText(new ByteArrayInputStream(result), line1.replace(line1.charAt(0), 'X'), false));
//
//        assertTrue(containsText(new ByteArrayInputStream(result), line2, true));
//        assertFalse(containsText(new ByteArrayInputStream(result), line2.replace(line2.charAt(0), 'X'), false));
//
//        assertTrue(containsText(new ByteArrayInputStream(result), line3, true));
//        assertFalse(containsText(new ByteArrayInputStream(result), line3.replace(line3.charAt(0), 'X'), false));

        //POST TESTING
        deletePDF(filename);

        FileOutputStream fileOutputStream = new FileOutputStream("new-pdf.pdf");
        fileOutputStream.write(result);
        fileOutputStream.close();
    }

//    public static boolean containsText(InputStream fileIn, String watermarkTextLine, boolean allPages) throws IOException {
//
//        PdfReader reader = new PdfReader(fileIn);
//        try{
//            int numPages = reader.getNumberOfPages();
//            for (int i = 1; i <= numPages; i++) {
//                StringWriter output = new StringWriter();
//                PrintWriter out = new PrintWriter(output);
//                RenderListener listener = new SimpleRenderListener(out);
//                PdfContentStreamProcessor processor = new PdfContentStreamProcessor(listener);
//
//                PdfDictionary pageDic = reader.getPageN(i);
//                PdfDictionary resourcesDic = pageDic.getAsDict(PdfName.RESOURCES);
//
//                processor.processContent(ContentByteUtils.getContentBytesForPage(reader, i), resourcesDic);
//                out.flush();
//                out.close();
//
//                if (output.toString().contains(watermarkTextLine)) {
//                    if (!allPages) {
//                        return true;
//                    }
//                } else if (allPages) {
//                    //if not found and we have to check if it is present in every page: false
//                    return false;
//                }
//            }
//
//            //If it is finished, depending of the type of query the method returns different results:
//            return allPages;
//        }finally{
//            reader.close();
//        }
//    }
//
//    /**
//     * Taken from Itext sample, simple and works...
//     */
//    private static class SimpleRenderListener implements RenderListener {
//
//        /**
//         * The print writer to which the information will be written.
//         */
//        private PrintWriter out;
//
//        /**
//         * Creates a RenderListener that will look for text.
//         */
//        public SimpleRenderListener(PrintWriter out) {
//            this.out = out;
//        }
//
//        /**
//         * @see RenderListener#beginTextBlock()
//         */
//        public void beginTextBlock() {
//            out.print("<");
//        }
//
//        /**
//         * @see RenderListener#endTextBlock()
//         */
//        public void endTextBlock() {
//            out.println(">");
//        }
//
//        /**
//         * @see RenderListener#renderImage(
//         *ImageRenderInfo)
//         */
//        public void renderImage(ImageRenderInfo renderInfo) {
//            //Do nothing because images are not rendered.
//        }
//
//        /**
//         * @see RenderListener#renderText(
//         *TextRenderInfo)
//         */
//        public void renderText(TextRenderInfo renderInfo) {
//            out.print(renderInfo.getText());
//        }
//    }


    public static File generatePdfFromText(String filename) throws PdfException {

        Document document = new Document(PageSize.A4);
        PdfWriter pdfWriter = null;
        File file = null;

        try {

            file = new File(filename);
            FileOutputStream fileout = new FileOutputStream(file);

            pdfWriter = PdfWriter.getInstance(document, fileout);

            document.open();

            Paragraph paragraph = new Paragraph();
            paragraph.add("Hello World");
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);

            document.close();

        } catch (DocumentException | IOException e1) {
            throw new PdfException(e1);
        } finally {
            document.close();
            if (pdfWriter != null) {
                pdfWriter.close();
            }
        return file;
     }

    }

    public static void deletePDF(String filename) {

        File file = null;
            file = new File(filename);
            if (file.exists()){
                file.delete();
            }

    }

}
