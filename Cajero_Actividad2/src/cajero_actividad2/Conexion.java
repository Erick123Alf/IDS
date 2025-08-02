package cajero_actividad2;


import java.sql.Connection;
import java.sql.DriverManager;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author PC
 */
public class Conexion {
    public Connection getConection(){
        Connection con = null;
        String base = "bdcajero";
        String url = "jdbc:mysql://localhost:3306/" + base;
        String user = "root";
        String passwd = "";
        
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = (Connection) DriverManager.getConnection(url, user, passwd);
            
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", " " +base);
        } 
        catch(Exception e){
            System.err.print(e);
        }
        return con;
    }
}
