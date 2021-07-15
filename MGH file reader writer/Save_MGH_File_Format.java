import java.awt.*;
import ij.*;
import java.io.*;

import ij.io.*;
import ij.util.Tools;
import ij.plugin.*;

//package ij.io;
import java.awt.*;
import java.io.*;
//import java.util.zip.*;
//import ij.*;
//import ij.process.*;
//import ij.measure.Calibration;
//import ij.plugin.filter.Analyzer;
//import ij.plugin.frame.Recorder;
//import ij.plugin.JpegWriter;
//import ij.plugin.Orthogonal_Views;
//import ij.gui.*;
//import ij.measure.Measurements;
//import javax.imageio.*;

public class Save_MGH_File_Format implements PlugIn {  
    
    public void run(String arg) {  
        ImagePlus imp = WindowManager.getCurrentImage();  
        if (null == imp) return;  
        SaveDialog sd = new SaveDialog("Save .mgh", "untitled", null);  
        String dir = sd.getDirectory();  
        if (null == dir) return; // user canceled dialog  
        dir = dir.replace('\\', '/'); // Windows safe  
        if (!dir.endsWith("/")) dir += "/";  
        saveMGH(imp, dir, sd.getFileName());  
    }  
    
    static public void saveMGH(ImagePlus imp, String direc, String fname) {  
        String path = direc + fname;
        File file = new File(path);  
        DataOutputStream dos = null;  
        try {  
            dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));  
            
            // read data:  
            FileInfo fi = imp.getFileInfo();  
            
            // HEADER: ... read all header tags and metadata  
            int mghversion = 0;
            int type = 0;
            int width = 0;
            int height = 0;
            int n_slices = 0; 
            
            byte a = 0;
            byte b = 0;
            byte c = 0;
            byte d = 0;
            
            if (fi.fileType == FileInfo.GRAY8)
                type = 0;
            if (fi.fileType == FileInfo.GRAY16_UNSIGNED)
                type = 1;
            if (fi.fileType == FileInfo.GRAY32_FLOAT)
                type = 2;
            
            width = fi.width;
            height = fi.height;
            n_slices = fi.nImages;
            
            a = (byte)(mghversion & 0xff);
            b = (byte)((mghversion >>> 8) & 0xff);
            c = (byte)((mghversion >>> 16) & 0xff);
            d = (byte)((mghversion >>> 24) & 0xff);
            dos.write(a);
            dos.write(b);
            dos.write(c);
            dos.write(d);
            
            a = (byte)(type & 0xff);
            b = (byte)((type >>> 8) & 0xff);
            c = (byte)((type >>> 16) & 0xff);
            d = (byte)((type >>> 24) & 0xff);
            dos.write(a);
            dos.write(b);
            dos.write(c);
            dos.write(d);
            
            a = (byte)(width & 0xff);
            b = (byte)((width >>> 8) & 0xff);
            c = (byte)((width >>> 16) & 0xff);
            d = (byte)((width >>> 24) & 0xff);
            dos.write(a);
            dos.write(b);
            dos.write(c);
            dos.write(d);
            
            a = (byte)(height & 0xff);
            b = (byte)((height >>> 8) & 0xff);
            c = (byte)((height >>> 16) & 0xff);
            d = (byte)((height >>> 24) & 0xff);
            dos.write(a);
            dos.write(b);
            dos.write(c);
            dos.write(d);
            
            a = (byte)(n_slices & 0xff);
            b = (byte)((n_slices >>> 8) & 0xff);
            c = (byte)((n_slices >>> 16) & 0xff);
            d = (byte)((n_slices >>> 24) & 0xff);
            dos.write(a);
            dos.write(b);
            dos.write(c);
            dos.write(d);
            
            int temp = 0;
            
            for (int i=0; i<(1024*1024 - 5*4);i++) {
                a = (byte)(temp & 0xff);
                dos.write(a);
            }
            
            // BODY: ... read all stack slices (or single slice)  
            if (fi.nImages==1)
            {
                fi.intelByteOrder = true;
                boolean signed16Bit = false;
                short[] pixels = null;
                int n = 0;
                try {
                    signed16Bit = imp.getCalibration().isSigned16Bit();
                    if (signed16Bit) {
                        pixels = (short[])imp.getProcessor().getPixels();
                        n = imp.getWidth()*imp.getHeight();
                        for (int i=0; i<n; i++)
                            pixels[i] = (short)(pixels[i]-32768);
                    }
                    ImageWriter file2 = new ImageWriter(fi);
                    //OutputStream out = new BufferedOutputStream(new FileOutputStream(path));
                    file2.write(dos);
                }
                catch (IOException e) {
                    showErrorMessage(e);
                }
                if (signed16Bit) {
                    for (int i=0; i<n; i++)
                        pixels[i] = (short)(pixels[i]+32768);
                }
                
                fi.fileName = fname;
                fi.directory = direc;
                fi.description = null;
                imp.setTitle(fname);
                imp.setFileInfo(fi);

            }
            else
            {
                fi.intelByteOrder = true;
                boolean signed16Bit = false;
                Object[] stack = null;
                int n = 0;
                boolean virtualStack = imp.getStackSize()>1 && imp.getStack().isVirtual();
                if (virtualStack) {
                    fi.virtualStack = (VirtualStack)imp.getStack();
                    if (imp.getProperty("AnalyzeFormat")!=null) fi.fileName="FlipTheseImages";
                }
                try {
                    signed16Bit = imp.getCalibration().isSigned16Bit();
                    if (signed16Bit && !virtualStack) {
                        stack = (Object[])fi.pixels;
                        n = imp.getWidth()*imp.getHeight();
                        for (int slice=0; slice<fi.nImages; slice++) {
                            short[] pixels = (short[])stack[slice];
                            for (int i=0; i<n; i++)
                                pixels[i] = (short)(pixels[i]-32768);
                        }
                    }
                    ImageWriter file2 = new ImageWriter(fi);
                    //OutputStream out = new BufferedOutputStream(new FileOutputStream(path));
                    file2.write(dos);
                }
                catch (IOException e) {
                    showErrorMessage(e);
                }
                if (signed16Bit) {
                    for (int slice=0; slice<fi.nImages; slice++) {
                        short[] pixels = (short[])stack[slice];
                        for (int i=0; i<n; i++)
                            pixels[i] = (short)(pixels[i]+32768);
                    }
                }
                
                fi.fileName = fname;
                fi.directory = direc;
                fi.description = null;
                imp.setTitle(fname);
                imp.setFileInfo(fi);
                
            }
            
            dos.flush(); 
            dos.close();
            
        } catch (Exception e) {  
            e.printStackTrace();  
        }   
    }  
    
    static void showErrorMessage(IOException e) {
		String msg = e.getMessage();
		if (msg.length()>100)
			msg = msg.substring(0, 100);
		IJ.error("FileSaver", "An error occured writing the file.\n \n" + msg);
	}
    
 //   static private void updateImp(ImagePlus imp, FileInfo fi) {
//		imp.changes = false;
 //       String name = imp.getTitle();
//		if (name!=null) {
		//	fi.fileFormat = fileFormat;
	//		FileInfo ofi = imp.getOriginalFileInfo();
	//		if (ofi!=null) {
	//			if (ofi.openNextName==null) {
	//				fi.openNextName = ofi.fileName;
      //              fi.openNextDir = ofi.directory;
		//		} else {
	//				fi.openNextName = ofi.openNextName;
//					fi.openNextDir = ofi.openNextDir ;
//				}
//			}
//			fi.fileName = name;
//			fi.directory = directory;
			//if (fileFormat==fi.TIFF)
			//	fi.offset = TiffEncoder.IMAGE_START;
//			fi.description = null;
//			imp.setTitle(name);
//			imp.setFileInfo(fi);
//		}
//    }
}  
                                    
                                    
