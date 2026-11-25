package ec.edu.monster.paises.modelo;

/** Un pais guardado en la base de datos (entidad del modelo). */
public class Pais {
    private int id;
    private String codigo = "";
    private String nombre = "";
    private String capital = "";
    private String region = "";
    private String subregion = "";
    private long poblacion;
    private double area;
    private String moneda = "";
    private String bandera = "";

    public Pais() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo == null ? "" : codigo.trim().toUpperCase(); }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre == null ? "" : nombre.trim(); }
    public String getCapital() { return capital; }
    public void setCapital(String capital) { this.capital = capital == null ? "" : capital.trim(); }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region == null ? "" : region.trim(); }
    public String getSubregion() { return subregion; }
    public void setSubregion(String subregion) { this.subregion = subregion == null ? "" : subregion.trim(); }
    public long getPoblacion() { return poblacion; }
    public void setPoblacion(long poblacion) { this.poblacion = poblacion; }
    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda == null ? "" : moneda.trim(); }
    public String getBandera() { return bandera; }
    public void setBandera(String bandera) { this.bandera = bandera == null ? "" : bandera.trim(); }
}
