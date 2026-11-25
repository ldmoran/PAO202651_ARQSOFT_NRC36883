package ec.edu.monster.paises.movil.vista;

import ec.edu.monster.paises.movil.modelo.Pais;

/** Lo que las pantallas necesitan de la actividad principal. */
interface Anfitrion {
    void aviso(String texto, boolean error);

    /** Abre el formulario de un pais; null para crear uno nuevo. */
    void abrirDetalle(Pais pais);

    void cerrarDetalle(boolean huboCambios);

    void actualizarTotal();
}
