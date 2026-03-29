export class Foto {
    id_foto: string = '';
    nome_file: string = '';
    url: string = ''; // "https://asso.adozioniconsapevoli.it/..."
    didascalia?: string = ''; // Può essere null o undefined
}

export interface Carattere {
    id_caratteri: string;
    carattere: string;      // Es. "Me la cavo"
    contesto: string;       // Es. "GUINZAGLIO"
    icona: string;          // Es. "house.png"
    id_tipo_carattere: string; // "0" è la nota speciale, gli altri sono tratti standard
    note?: string;          // Per il tipo 0 contiene la descrizione breve
}

export class Animale {
    id_animale: string= '';
    cod_animale: string= '';
    nome: string= '';
    dt_nascita: string= '';
    sesso: string= ''; // 'M' o 'F'
    taglia: string= '';
    descr_breve: string= '';
    descr_lunga: string= '';
    location: string= '';
    eta: string= ''; // "Adulto", "Cucciolo"
    foto: Foto | null = null;  // Oggetto annidato
    coloreStato: string= ''; // Utile per il badge
    descr_stato: string= ''; // "Adottabile"
    razza?: string= ''; // Può essere null
    stato: string= '';
    specie: string= '';
    caratteristiche?: string= '';
    peso?: string= '';
    num_microchip?: string= '';
    periodo?: string= '';
    periodo_short?: string= '';
    id_razza?: string= '';
    tipo_razza?: string;
    sterilizzato?: string= '';
    proprietario?: string= '';
    regione?: string= '';
    id_tipo_animale?: string= '';
    id_colore?: string= '';
    id_stato?: string= '';
    tags: Tag[] = [];
    documenti: Documento[] = [];
    dataDecesso?: string = '';
}

export interface Tag {
    id_tag: string;
    tag: string;
}

export interface Documento {
    id_documento: string;
    id_tipo_documento: string;
    tipoDocumento: {
        id_tipo_documento: string;
        documento: string;
        ambito: any;
        prefix_filename: any;
    };
    num_documento: string;
    account: any;
    dt_inserimento: string;
    note: any;
    assoFiles: AssoFile[]; 
}

export interface AssoFile {
    id_file: string;
    filename: string;
    extension: string;
    size: string;
    id_documento: string;
    full_path: string;
    file?: any; // Può essere null come da JSON
}