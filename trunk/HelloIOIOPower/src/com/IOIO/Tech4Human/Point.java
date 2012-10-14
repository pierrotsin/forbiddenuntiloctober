package com.IOIO.Tech4Human;

import java.util.ArrayList;
import java.util.List;

public class Point
{
    List<Double> lat;
    
    List<Double> lon;
    
    List<Integer> index;
    
    public Point()
    {
        lat = new ArrayList<Double>();
        lon = new ArrayList<Double>();
        index = new ArrayList<Integer>();
    }
    
    public void clear()
    {
        lat.clear();
        lon.clear();
        index.clear();
    }
    
}
