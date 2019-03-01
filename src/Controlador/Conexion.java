/******************************************************
 *   UNIVERSIDAD POLITECNICA DEL ESTADO DE MORELOS    *
 *                                                    *
 *          CUATRIMESTRE: 6      GRUPO: A             *
 *  TIPO DE EVIDENCIA: INTEGRADORA                    *
 *  MATERIA: INGENIERIA DE SOFTWARE                   *
 *  PROFESORA: MIGUEL HIDALGO REYES                 *
 *                                                    *
 *              INTEGRANTES DEL EQUIPO:               *
 *         NOMBRE                      MATRICULA      *
 *                                                    *
 * ESQUIVEL MARTINEZ ESTEBAN          EMEO151863      *
 * NOLASCO ORTIZ ELIACIM              NOEO150589      *
 * VALDEPENA RIVERA FRANCISCO JAVIER  VRFO150252      *
 ******************************************************/

package Controlador;

import Modelo.Señal;
import Modelo.Usuario;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class Conexion {
    private String url = "";
    private String usuario = "root"; //Nombre de usuario SQL
    private String password = ""; //Contrasena del servidor MySQL
    private String nombreBase = "systema_servicios"; //Nombre de la base de datos
    Connection conect;
    
    public Connection Conexion(){ //Función para establecer la conexión con la base de datos
        try{
            Class.forName("com.mysql.jdbc.Driver");
            conect = DriverManager.getConnection("jdbc:mysql://localhost/"+nombreBase+"?user="+usuario+"&password="+password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conect;
    }
    
    public boolean agregar(Usuario nvoUsuario){ //Función para agregar un nuevo usuario
        String sql = "INSERT INTO usuario VALUES('"+0+"','"+nvoUsuario.getNombre()+"','"+nvoUsuario.getaPaterno()+"','"+nvoUsuario.getaMaterno()+"','"+nvoUsuario.getNickname()+"','"+nvoUsuario.getPassword()+"','"+nvoUsuario.getEmail()+"','"+nvoUsuario.getTipo()+"','"+nvoUsuario.getActivo()+"')";
        try {
            Statement stm = null;
            
            stm = Conexion().createStatement();
            stm.executeUpdate(sql);
            JOptionPane.showMessageDialog(null,"El usuario se registro correctamente","Registro exitoso",JOptionPane.PLAIN_MESSAGE);
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public String acceder(String nickname, String password){ //Función para verificar que un usuario exista en la base de datos
        String sql = "SELECT * FROM usuario WHERE nickname='"+nickname+"' && password='"+password+"' && activo=1";
        String tipo = null;
        try {
            Statement stm = Conexion().createStatement();
            ResultSet rs = stm.executeQuery(sql);
            boolean existe = false;
            
            while(rs.next()){
                existe = true;
                tipo = rs.getString(8);
            }
            
            if(!existe){
                JOptionPane.showMessageDialog(null,"Usuario o Contrasena incorrectos","Datos incorrectos",JOptionPane.ERROR_MESSAGE);
            }else{
                return tipo;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tipo;
    }
    
    public boolean buscarUsuario(String nickname){ //Función para verificar que el nombre de un usuario exista
        String sql = "SELECT * FROM usuario WHERE nickname='"+nickname+"'";
        try {
            Statement stm = Conexion().createStatement();
            ResultSet rs = stm.executeQuery(sql);
            boolean existe = false;
            
            while(rs.next())
                existe = true;
            
            if(existe){
                JOptionPane.showMessageDialog(null,"El nickname que introdujo ya esta siendo utilizado","Usuario existente",JOptionPane.ERROR_MESSAGE);
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public boolean eliminar(String nickname){ //Función para eliminar un usuario
        String sql = "DELETE FROM usuario WHERE nickname='"+nickname+"'";
        try {
            Statement stm = null;
            
            stm = Conexion().createStatement();
            stm.execute(sql);
            JOptionPane.showMessageDialog(null,"El usuario se elimino correctamente","Registro exitoso",JOptionPane.PLAIN_MESSAGE);
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public boolean modificarUsuario(String nickname, Usuario usr){ //Función para modificar un usuario
        String sql = "UPDATE usuario set nombre = '"+usr.getNombre()+"', apellidoP = '"+usr.getaPaterno()+"', apellidoM = '"+usr.getaMaterno()+"', password = '"+usr.getPassword()+"', activo = '"+((usr.getActivo()==true)?1:0)+"', tipo = '"+usr.getTipo()+"', email = '"+usr.getEmail()+"', nickname = '"+usr.getNickname()+"' WHERE nickname = '"+nickname+"'";
        
        try {
            Statement stm = null;
            
            stm = Conexion().createStatement();
            if(!stm.execute(sql)){
                JOptionPane.showMessageDialog(null,"Los cambios se realizaron correctamente","Cambios realizados",JOptionPane.PLAIN_MESSAGE);
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public Usuario buscarUsr(String nickname){ //Función para recuperar la informacion de un usuario en especifico
        String sql = "SELECT * FROM usuario WHERE nickname='"+nickname+"'";
        Usuario usr = new Usuario();
        try {
            Statement stm = Conexion().createStatement();
            ResultSet rs = stm.executeQuery(sql);
            
            while(rs.next()){
                usr.setNombre(rs.getString(2));
                usr.setaPaterno(rs.getString(3));
                usr.setaMaterno(rs.getString(4));
                usr.setNickname(rs.getString(5));
                usr.setPassword(rs.getString(6));
                usr.setEmail(rs.getString(7));
                usr.setTipo(rs.getString(8));
                usr.setActivo(rs.getBoolean(9));
            }
         
            return usr;
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public int buscarIdUsuario(String nickname){
        String sql = "SELECT idUsuario FROM usuario WHERE nickname='"+nickname+"'";
        int idUsuario=-1;
        try {
            Statement stm = Conexion().createStatement();
            ResultSet rs = stm.executeQuery(sql);
            
            while(rs.next())
                idUsuario = rs.getInt(1);

            return idUsuario;
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idUsuario;
    }
    
    public String buscarContraseña(String email){
        String sql = "SELECT * FROM usuario WHERE email='"+email+"' && activo=1";
        String password = null;
        try {
            Statement stm = Conexion().createStatement();
            ResultSet rs = stm.executeQuery(sql);
            
            while(rs.next())
                password = rs.getString(6);
            
            
            if(password==null){
                JOptionPane.showMessageDialog(null,"No existe ningun usuario con ese correo","Datos incorrectos",JOptionPane.ERROR_MESSAGE);
            }else{
                return password;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return password;
    }
    
    //--------------Gestionar señales de trafico
    public int getIdAudio(){
        String sql = "SELECT * FROM audio ORDER by idAudio DESC LIMIT 1";
        int id = -1;
        try {
            Statement stm = Conexion().createStatement();
            ResultSet rs = stm.executeQuery(sql);
            
            while(rs.next())
                id = rs.getInt(1);
            
            return id;
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }
    
    public boolean agregarSeñal(String nombre, String rutaAudio, String rutaImagen){
        String sqlAudio = "INSERT INTO audio VALUES('"+0+"','"+nombre+"','"+rutaAudio+"')";
        String sqlSeñal = "";
        try {
            Statement stm = null;
            
            stm = Conexion().createStatement();
            stm.executeUpdate(sqlAudio);
            
            int idAudio = getIdAudio();
            
            if(idAudio!=-1){
                sqlSeñal = "INSERT INTO imagen VALUES('"+0+"','"+nombre+"','"+rutaImagen+"','1','"+idAudio+"')";
                stm = Conexion().createStatement();
                stm.executeUpdate(sqlSeñal);
            }
            
            JOptionPane.showMessageDialog(null,"La señal fue agregada correctamente","Registro exitoso",JOptionPane.PLAIN_MESSAGE);
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public void obtenerSeñales(JTable tblSeñales){
        DefaultTableModel modelo = new DefaultTableModel();
        modelo.setColumnIdentifiers(new Object[]{"Id","Nombre","Ruta","Audio"});
        String sql = "SELECT idImagen, Imagen.nombre, Imagen.ruta, Audio.ruta FROM Imagen, Audio WHERE Audio_idAudio = idAudio AND estado = 1";
        try {
            Statement stm = Conexion().createStatement();
            ResultSet rs = stm.executeQuery(sql);
            
            while(rs.next()){
                modelo.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3).replace('%', '\\'),rs.getString(4).replace('%', '\\')});
            }
            
            tblSeñales.setModel(modelo);
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void obtenerBitacora(JTable tblBitacora, String busqueda, String fecha, String usuario, String señal){
        DefaultTableModel modelo = new DefaultTableModel();
        modelo.setColumnIdentifiers(new Object[]{"Id","Nickname","Fecha","Hora","Señal","Ruta","Estado señal"});
        String sql = obtenerSql(busqueda,fecha,usuario,señal);
        try {
            Statement stm = Conexion().createStatement();
            ResultSet rs = stm.executeQuery(sql);
            
            while(rs.next()){
                modelo.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getDate(3).toString(),rs.getTime(4).toString(),rs.getString(5),rs.getString(6).replace('%', '\\'),(rs.getInt(7)==1)?"Activa":"Inactiva"});
            }
            
            tblBitacora.setModel(modelo);
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String obtenerSql(String busqueda, String fecha, String usuario, String señal){
        String sql = "SELECT idBitacora, nickname, fecha, hora, Imagen.nombre, ruta, Imagen.estado FROM Usuario, Bitacora, Imagen WHERE idUsuario = Usuario_idUsuario AND Imagen_idImagen = idImagen ";
                   
        switch(busqueda){
            case "Fecha": //Busqueda por fecha
                sql = "SELECT idBitacora, nickname, fecha, hora, Imagen.nombre, ruta, Imagen.estado FROM Usuario, Bitacora, Imagen WHERE idUsuario = Usuario_idUsuario AND Imagen_idImagen = idImagen "
                     +" AND fecha = '"+fecha+"'";
                break; 
            case "Usuario": //Busqueda por Usuario
                sql = "SELECT idBitacora, nickname, fecha, hora, Imagen.nombre, ruta, Imagen.estado FROM Usuario, Bitacora, Imagen WHERE idUsuario = Usuario_idUsuario AND Imagen_idImagen = idImagen "
                     +" AND nickname = '"+usuario+"'";
                break;
            case "Señal": //Busqueda por Señal
                sql = "SELECT idBitacora, nickname, fecha, hora, Imagen.nombre, ruta, Imagen.estado FROM Usuario, Bitacora, Imagen WHERE idUsuario = Usuario_idUsuario AND Imagen_idImagen = idImagen "
                     +" AND Imagen.nombre = '"+señal+"'";
                break;
        }
        return sql;
    }
    
    public void obtenerUsuarios(JComboBox cbxUsuarios){
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        String sql = "SELECT nickname FROM Usuario WHERE tipo != 'admin'";
        try {
            Statement stm = Conexion().createStatement();
            ResultSet rs = stm.executeQuery(sql);
            
            while(rs.next()){
                model.addElement(rs.getString(1));
            }
            
            cbxUsuarios.setModel(model);
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void obtenerSeñales(JComboBox cbxSeñales){
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        String sql = "SELECT nombre FROM Imagen";
        try {
            Statement stm = Conexion().createStatement();
            ResultSet rs = stm.executeQuery(sql);
            
            while(rs.next()){
                model.addElement(rs.getString(1));
            }
            
            cbxSeñales.setModel(model);
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ArrayList<Señal> obtenerSeñalesActivas(){ //Se obtienen las señales para realizar la detección
        ArrayList<Señal> señales = new ArrayList();
        Señal nvaSeñal;
        String sql = "SELECT idImagen, Imagen.nombre, Imagen.ruta, Audio.ruta FROM Imagen, Audio WHERE Audio_idAudio = idAudio AND estado = 1";
        try {
            Statement stm = Conexion().createStatement();
            ResultSet rs = stm.executeQuery(sql);
            
            while(rs.next()){
                nvaSeñal = new Señal();
                nvaSeñal.setId(rs.getInt(1));
                nvaSeñal.setNombre(rs.getString(2));
                nvaSeñal.setUrlImage(rs.getString(3).replace('%', '\\'));
                nvaSeñal.setUrlAudio(rs.getString(4).replace('%', '\\'));
                señales.add(nvaSeñal);
            }
            
            return señales;
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public boolean modificarSeñal(int id, String nombre, String urlAudio){
        String sql = "UPDATE Imagen set nombre = '"+nombre+"' WHERE idImagen = '"+id+"'";
        
        try {
            Statement stm = null;
            
            stm = Conexion().createStatement();
            if(!stm.execute(sql)){
                if(urlAudio.isEmpty() && !modificarAudio(id,urlAudio)){
                    return true;
                }else{
                    if(!urlAudio.isEmpty() && modificarAudio(id,urlAudio)){
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public int obtenerIdAudio(int idSeñal){
        String sql = "SELECT Audio_idAudio FROM Imagen WHERE idImagen = '"+idSeñal+"'";
        int id = -1;
        try {
            Statement stm = Conexion().createStatement();
            ResultSet rs = stm.executeQuery(sql);

            while(rs.next())
                id = rs.getInt(1);    
            
            return id;
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }
    
    public boolean modificarAudio(int idSeñal, String urlAudio){
        if(!urlAudio.isEmpty()){
            int id = obtenerIdAudio(idSeñal);
            if(id!=-1){
                String sql = "UPDATE Audio set ruta = '"+urlAudio+"' WHERE idAudio = '"+id+"'";
        
                try {
                    Statement stm = null;

                    stm = Conexion().createStatement();
                    if(!stm.execute(sql))
                        return true;
                } catch (SQLException ex) {
                    Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return false;
    }
    
    public boolean señalDuplicada(String nombre){ //Función para verificar que el nombre de un usuario exista
        String sql = "SELECT * FROM imagen WHERE nombre='"+nombre+"' AND estado = 1";
        try {
            Statement stm = Conexion().createStatement();
            ResultSet rs = stm.executeQuery(sql);
            boolean existe = false;
            
            while(rs.next())
                existe = true;
            
            if(existe){
                JOptionPane.showMessageDialog(null,"El nombre de la señal ya existe","Usuario existente",JOptionPane.ERROR_MESSAGE);
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public boolean eliminarSeñal(int idSeñal){
        String sql = "UPDATE Imagen set estado = 0 WHERE idImagen = '"+idSeñal+"'";
        try {
            Statement stm = null;

            stm = Conexion().createStatement();
            if(!stm.execute(sql))
                return true;
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public void obtenerUsuarios(JTable tblUsuarios){
        DefaultTableModel modelo = new DefaultTableModel();
        modelo.setColumnIdentifiers(new Object[]{"Id","Nickname"});
        String sql = "SELECT * FROM Usuario WHERE tipo = 'usuario'";
        try {
            Statement stm = Conexion().createStatement();
            ResultSet rs = stm.executeQuery(sql);
            
            while(rs.next()){
                modelo.addRow(new Object[]{rs.getInt(1),rs.getString(5)});
            }
            
            tblUsuarios.setModel(modelo);
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean registrarBitacora(int idUsuario, int idSeñal, String fecha, String hora){
        String sql = "INSERT INTO bitacora VALUES ('"+0+"','"+fecha+"','"+hora+"','"+idUsuario+"','"+idSeñal+"')";
        try {
            Statement stm = null;
            
            stm = Conexion().createStatement();
            if(!stm.execute(sql))
                return true;
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
