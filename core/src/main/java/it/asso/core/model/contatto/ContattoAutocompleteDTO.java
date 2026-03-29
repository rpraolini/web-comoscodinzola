package it.asso.core.model.contatto;

public class ContattoAutocompleteDTO {

    private String id_contatto;
    private String nome;
    private String cognome;
    private String rag_sociale;
    private String desc_provincia;
    private String email;
    private String telefono_1;
    private String cellulare;
    private String id_tipo_contatto;
    private String blacklist;

    public String getId_contatto() { return id_contatto; }

    public void setId_contatto(String id_contatto) { this.id_contatto = id_contatto; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getRag_sociale() { return rag_sociale; }
    public void setRag_sociale(String rag_sociale) { this.rag_sociale = rag_sociale; }

    public String getDesc_provincia() { return desc_provincia; }
    public void setDesc_provincia(String desc_provincia) { this.desc_provincia = desc_provincia; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono_1() { return telefono_1; }
    public void setTelefono_1(String telefono_1) { this.telefono_1 = telefono_1; }

    public String getCellulare() { return cellulare; }
    public void setCellulare(String cellulare) { this.cellulare = cellulare; }

    public String getId_tipo_contatto() { return id_tipo_contatto; }
    public void setId_tipo_contatto(String id_tipo_contatto) { this.id_tipo_contatto = id_tipo_contatto; }

    public String getBlacklist() { return blacklist; }
    public void setBlacklist(String blacklist) { this.blacklist = blacklist; }
}