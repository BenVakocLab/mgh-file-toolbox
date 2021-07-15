import java.awt.*;
import ij.*;
import java.io.*;

import ij.io.*;
import ij.util.Tools;
import ij.plugin.*;


public class Open_MGH_File_Format extends ImagePlus implements PlugIn {  
      
    public void run(String arg) {  
        String path = getPath(arg);  
        if (null == path) return;  
        if (!parse(path)) return;  
        if (null == arg || 0 == arg.trim().length()) this.show(); // was opened by direct call to the plugin  
                          // not via HandleExtraFileTypes which would  
                          // have given a non-null arg.  
    }  
  
    /** Accepts URLs as well. */  
    private String getPath(String arg) {  
        if (null != arg) {  
            if (0 == arg.indexOf("http://")  
             || new File(arg).exists()) return arg;  
        }  
        // else, ask:  
        OpenDialog od = new OpenDialog("Choose a .mgh file", null);  
        String dir = od.getDirectory();  
        if (null == dir) return null; // dialog was canceled  
        dir = dir.replace('\\', '/'); // Windows safe  
        if (!dir.endsWith("/")) dir += "/";  
        return dir + od.getFileName();  
    }  
  
    /** Opens URLs as well. */  
    private InputStream open(String path) throws Exception {  
        if (0 == path.indexOf("http://"))  
            return new java.net.URL(path).openStream();  
        return new FileInputStream(path);  
    }  
  
    private boolean parse(String path) {  
        // Open file and read header  
        byte[] buf = new byte[1024];  
        try {  
            InputStream stream = open(path);  
            stream.read(buf, 0, 1024);  
            stream.close();  
        } catch (Exception e) {  
            e.printStackTrace();  
            return false;  
        }  
        // Read width,height,slices ... from the header  
        /* THIS IS AN EXAMPLE */  
        int mghversion = readIntLittleEndian(buf, 0);
        int type = 0;
        int width = 0;
        int height = 0;
        int n_slices = 0;
        if (mghversion == 0)
        {
            type = readIntLittleEndian(buf, 4);
            width = readIntLittleEndian(buf, 8);  
            height = readIntLittleEndian(buf, 12);  
            n_slices = readIntLittleEndian(buf, 16);
        }
     
        // Build a new FileInfo object with all file format parameters and file data  
        FileInfo fi = new FileInfo();
        if (type == 0)
            fi.fileType = FileInfo.GRAY8;
        if (type == 1)
            fi.fileType = FileInfo.GRAY16_UNSIGNED;
        if (type == 2)
            fi.fileType = FileInfo.GRAY32_FLOAT;
        fi.fileFormat = fi.RAW;  
        int islash = path.lastIndexOf('/');  
        if (0 == path.indexOf("http://")) {  
            fi.url = path;  
        } else {  
            fi.directory = path.substring(0, islash+1);  
        }  
        fi.fileName = path.substring(islash+1);  
        fi.width = width;  
        fi.height = height;  
        fi.nImages = n_slices;  
        fi.gapBetweenImages = 0;  
        fi.intelByteOrder = true; // little endian  
        fi.whiteIsZero = false; // no inverted LUT  
        fi.longOffset = fi.offset = 1048576; // header size, in bytes  
  
        // Now make a new ImagePlus out of the FileInfo  
        // and integrate its data into this PlugIn, which is also an ImagePlus  
        try {  
            FileOpener fo = new FileOpener(fi);  
            ImagePlus imp = fo.open(false);  
            this.setStack(imp.getTitle(), imp.getStack());  
            this.setCalibration(imp.getCalibration());  
            Object obinfo = imp.getProperty("Info");  
            if (null != obinfo) this.setProperty("Info", obinfo);  
            this.setFileInfo(imp.getOriginalFileInfo());  
        } catch (Exception e) {  
            e.printStackTrace();  
            return false;  
        }  
        return true;  
    }  
  
    private final int readIntLittleEndian(byte[] buf, int start) {
        return (int) (0x000000FF & ((int)buf[start])) + ((0x000000FF & ((int)buf[start+1]))<<8) + ((0x000000FF & ((int)buf[start+2]))<<16) + ((0x000000FF & ((int)buf[start+3]))<<24); 
    }  
}  
      