package com.hust.indoorlocation.tools.util;

import static java.lang.Math.cos;

import com.baidu.mapapi.model.LatLng;

/**
 * @author admin
 */
public class LatLngUtil {
    static double PI = Math.PI;
    static double Rc = 6378137;  // 赤道半径
    static double Rj = 6356725;  // 极半径

    double m_Longitude, m_Latitude;
    double m_RadLo, m_RadLa;
    double Ec;
    double Ed;

    public LatLngUtil(double longitude, double latitude)
    {
        m_Longitude = longitude;
        m_Latitude = latitude;
        m_RadLo = longitude * PI/180.;
        m_RadLa = latitude * PI/180.;
        Ec = Rj + (Rc - Rj) * (90.-m_Latitude) / 90.;
        Ed = Ec * cos(m_RadLa);
    }

    public static LatLngUtil GetJWDB(LatLngUtil A, double x, double y) {

        double dx = x;
        double dy = y;

        double BJD = (dx / A.Ed + A.m_RadLo) * 180. / PI;
        double BWD = (dy / A.Ec + A.m_RadLa) * 180. / PI;

        LatLngUtil B = new LatLngUtil(BJD, BWD);

        return B;
    }

    public static LatLng GetJWDB(LatLng A, double x, double y) {
        double dx = x;
        double dy = y;

        LatLngUtil latLngA=new LatLngUtil(A.longitude,A.latitude);

        double BJD = (dx / latLngA.Ed + latLngA.m_RadLo) * 180. / PI;
        double BWD = (dy / latLngA.Ec + latLngA.m_RadLa) * 180. / PI;

        LatLngUtil B = new LatLngUtil(BJD, BWD);
        LatLng latLngB=new LatLng(B.m_Latitude,B.m_Longitude);
        return latLngB;
    }

    @Override
    public String toString() {
        return "LatLngHust{" +
                "m_Longitude=" + m_Longitude +
                ", m_Latitude=" + m_Latitude +
                '}';
    }
}
