package it.asso.core.dto.proprietario;

import it.asso.core.model.contatto.Contatto;

public class ProprietarioDTO {

    private String id_contatto;
    private String nome;
    private String cognome;
    private String rag_sociale;
    private String desc_provincia;
    private String telefono_1;
    private String cellulare;
    private String email;
    private String dt_proprietario;

    // Costruttore da Contatto
    public static ProprietarioDTO from(Contatto contatto) {
        if (contatto == null) return null;
        ProprietarioDTO dto = new ProprietarioDTO();
        dto.id_contatto = contatto.getId_contatto();
        dto.nome = contatto.getNome();
        dto.cognome = contatto.getCognome();
        dto.rag_sociale = contatto.getRag_sociale();
        dto.desc_provincia = contatto.getDesc_provincia();
        dto.telefono_1 = contatto.getTelefono_1();
        dto.cellulare = contatto.getCellulare();
        dto.email = contatto.getEmail();
        return dto;
    }



    public String getDt_proprietario() { return dt_proprietario; }
    public void setDt_proprietario(String dt_proprietario) { this.dt_proprietario = dt_proprietario; }
    // getters
    public String getId_contatto() { return id_contatto; }
    public String getNome() { return nome; }
    public String getCognome() { return cognome; }
    public String getRag_sociale() { return rag_sociale; }
    public String getDesc_provincia() { return desc_provincia; }
    public String getTelefono_1() { return telefono_1; }
    public String getCellulare() { return cellulare; }
    public String getEmail() { return email; }
}
