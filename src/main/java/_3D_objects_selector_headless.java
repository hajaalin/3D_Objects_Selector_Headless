/*
 * _3D_objects_selector_headless.java
 *
 * Created on 7 October 2013 by Harri Jäälinoja.
 *
 * Copyright (C) 2013 Harri Jäälinoja
 *
 * 
 * This code is based on: 
 *
 * _3D_objects_counter.java
 * Created on 7 novembre 2007, 11:54
 * Copyright (C) 2007 Fabrice P. Cordelières
 *  
 * License:
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */


import ij.*;
import ij.ImagePlus.*;
import ij.process.*;
import ij.gui.*;
import ij.util.*;
import java.util.*;

import Utilities.*;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;

/**
 *
 * @author Harri Jäälinoja (harri.jaalinoja@helsinki.fi)
 * @version 0.1, 2013-10-07
 */
public class _3D_objects_selector_headless implements ExtendedPlugInFilter {
    ImagePlus image;
    ImageProcessor ip;
    int width, height, nbSlices, length;
    double min, max;
    String title, redirectTo;
    int thr = 0; // expect binary image
    boolean excludeOnEdges, redirect;

    int objectSizeMin = 0;
    int objectSizeMax = Integer.MAX_VALUE;
    private boolean editCurrent = true;

    private boolean paramsReceived = false;
    private int flags = DOES_8G | NO_CHANGES;

    public int setup(String arg, ImagePlus image) {
	this.image = image;
        System.out.println("_3D_objects_counter_headless.setup arg=" + arg);
        
        if (!arg.equals("")) {
            
            String[] params = arg.split(",");
            String[] keyval;
            for (int i=0;i<params.length;i++) {
                keyval = params[i].split("=");
		System.out.printf("%s %s", keyval[0],keyval[1]);
                if (keyval[0].equals("objSizeMin")) {
                    this.objectSizeMin = Integer.valueOf(keyval[1]).intValue();
                    // if objMinSize is given in setup, we can assume that the user is running this from a script
                    this.paramsReceived = true;
                }
            }
        }
	
	return flags;
    }

    public void setNPasses(int n) {
    }

    public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
        // if got all parameters in setup, skip the dialog
        if (paramsReceived) return flags;
        
        GenericDialog gd=new GenericDialog("3D Object Selector v0.0");
        
        gd.addMessage("Size filter: ");
        gd.addNumericField("Min.",objectSizeMin, 0);
        gd.addNumericField("Max.", objectSizeMax, 0);
        gd.addCheckbox("Edit current map", editCurrent);
               
        gd.showDialog();
        if (gd.wasCanceled()){
            return DONE;
        }
        
        this.objectSizeMin=(int) gd.getNextNumber();
        this.objectSizeMax=(int) gd.getNextNumber();
        this.editCurrent = gd.getNextBoolean();
        
        return flags;
    }
    
    public void run(ImageProcessor ip) {
        if (editCurrent) {
            ImagePlus result = compute(image);
            // modify the original image
            this.image.setStack(result.getStack());
        }
        else {
            compute(image).show();
        }
    }

    public ImagePlus compute(ImagePlus imp) {
	System.out.printf("objectSizeMin=%s",new Integer(this.objectSizeMin).toString());
        if (IJ.versionLessThan("1.39i")) return null;
        
        if (imp==null){
            IJ.error("You need to open an image first.");
        }
        
        width=imp.getWidth();
        height=imp.getHeight();
        nbSlices=imp.getStackSize();

        Counter3D OC=new Counter3D(imp, thr, objectSizeMin, objectSizeMax, excludeOnEdges, redirect);
        
	return OC.getObjMap();
    }

}