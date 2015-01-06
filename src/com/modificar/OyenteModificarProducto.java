/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.modificar;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import com.puntoVenta.Conexion;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author JR
 */
public class OyenteModificarProducto implements ActionListener{
    ModificarProducto mp;
    Conexion c;
    private JFileChooser jfc;
    private FileNameExtensionFilter filter = new FileNameExtensionFilter("Imagenes", "png", "jpeg", "jpg", "gif");
    private String nombreArchivo;
    private String ruta;
    private String destino;
    
    OyenteModificarProducto(Conexion c, ModificarProducto mp) {
        this.mp = mp;
        this.c = c;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch(e.getActionCommand()){
            case "Cancelar":
                System.out.println("cancelar1");
                if(JOptionPane.showConfirmDialog(null, "Seguro que quieres cancelar", "Aviso", JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION)
                    mp.dispose();
                break;
            case "Seleccionar ruta":
                jfc = new JFileChooser();
                jfc.setFileFilter(filter);
                int opcion = jfc.showOpenDialog(jfc);
                if (opcion == JFileChooser.APPROVE_OPTION) {
                    try {
                        nombreArchivo = jfc.getSelectedFile().getName();
                        ruta = jfc.getSelectedFile().getPath();
                        ImageIcon aux = new ImageIcon(ruta);
                        ImageIcon img = new ImageIcon(aux.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH));
                        mp.getImagen().setIcon(img);
//                        a.getNombreImagen().setText(nombreArchivo);
                        javax.swing.SwingUtilities.updateComponentTreeUI(mp);
                        Path rutaFROM = (Path) Paths.get(ruta);
                        destino = "src/img/productos/" + nombreArchivo;
                        Path rutaTo = (Path) Paths.get(destino);
                        CopyOption[] options = new CopyOption[]{
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.COPY_ATTRIBUTES
                        };
                        Files.copy(rutaFROM, rutaTo, options);

                    } catch (IOException ex) {
                        System.out.println(ex);
                    }
                    
                }
            break;
                
            case "Modificar":
                System.out.println("Update1");
                if(ejecutarConsulta(true)){
                    JOptionPane.showConfirmDialog(null, "Se modifico con exito", "Exito", JOptionPane.DEFAULT_OPTION);
                }
                else
                    JOptionPane.showMessageDialog(null, "Error en la modificacion", "Error", JOptionPane.DEFAULT_OPTION);
                mp.dispose();
                break;
            
            case "Eliminar":
                System.out.println("Drop1");
                if(JOptionPane.showConfirmDialog(null, "Seguro que quieres eliminar", "Aviso", JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION){
                    if(ejecutarConsulta(false)){
                    JOptionPane.showMessageDialog(null, "Se elimino con exito", "Exito", JOptionPane.DEFAULT_OPTION);
                    }
                    else
                        JOptionPane.showMessageDialog(null, "Error en la modificacion", "Alerta", JOptionPane.DEFAULT_OPTION);
                    mp.dispose();
            
                }
                
                
                break;
        }
    }
    
    public boolean ejecutarConsulta(boolean hacer){
        String consulta, preconsulta="", preconsulta2="";
        c.iniciarConexion();
        if(hacer){
            consulta = "update punto_venta.Producto set nombreProducto='"+
                    mp.getNombre().getText()+"', precio='"+mp.getPrecio().getText()+
                    "', rutaImagen='"+destino+
                    "', descripcionProducto='"+mp.getDescripcion().getText().trim()+
                    "', existencias='"+mp.getExistencia().getText()
                    + "' where idProducto ="
                    +mp.getIds().get(mp.getProductos().getSelectedIndex()).toString()+";";
        }
        else{
            preconsulta = "DELETE Detalle_fact FROM Detalle_fact, Producto\n" +
            "WHERE Detalle_fact.Producto_idProducto =Producto.idProducto\n" +
            "and Producto.idProducto="+mp.getIds().get(mp.getProductos().getSelectedIndex()).toString()+";";
            preconsulta2= "delete Guarda from Guarda, Producto\n" +
            "where Guarda.Producto_idProducto = Producto.idProducto and\n" +
            "Producto.idProducto="+mp.getIds().get(mp.getProductos().getSelectedIndex()).toString()+";";
            consulta = "delete from punto_venta.Producto where idProducto="
                    +mp.getIds().get(mp.getProductos().getSelectedIndex()).toString()+";";
            
        }
        try {
            if(!preconsulta.equals("")&&!preconsulta2.equals("")){
                c.getStament().execute(preconsulta);
                c.getStament().execute(preconsulta2);
            }
            System.out.println(consulta);
            if(c.getStament().execute(consulta))
                return true;
        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null, "Asegurate de eliminar registros\nde detalle de factura y de de cabecera de factura antes"
                    + " de hacer esta operacion", "Alerta", JOptionPane.DEFAULT_OPTION);
            System.out.println(ex);
            return false;
        }
//        catch (SQLException ex) {
//            JOptionPane.showMessageDialog(null, "Ocurrio un error "+ex.toString(), "Alerta", JOptionPane.DEFAULT_OPTION);
//            System.out.println(ex);
//            return false;
//        }
        return true;
    }
    
}
