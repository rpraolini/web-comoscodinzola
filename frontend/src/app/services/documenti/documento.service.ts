import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';


@Injectable({
    providedIn: 'root'
})
export class DocumentiService {

    private apiUrl = environment.apiUrl + '/private/documenti';

    constructor(private http: HttpClient) { }


    getTipiDoc(ambito: string): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/${ambito}`);
    }

    saveDocumentoAnimale(idAnimale: string, tipoDoc: any, file: File): Observable<any> {
        const formData = new FormData();

        // Prepariamo l'oggetto documento come stringa JSON, come richiesto dal Controller Java
        const documentoJson = JSON.stringify({
            id_tipo_documento: tipoDoc.id_tipo_documento,
            documento: tipoDoc.descr_tipo_documento
        });

        formData.append('idAnimale', idAnimale);
        formData.append('documento', documentoJson);
        formData.append('file', file); // Il nome del parametro deve coincidere con @RequestParam("file")

        return this.http.post(`${this.apiUrl}/saveDocumentoAnimale`, formData);
    }

    deleteDocumento(idDocumento: string): Observable<any> {
        // Passiamo l'ID come parametro di query per far sì che il Controller lo riceva correttamente
        return this.http.delete(`${this.apiUrl}/deleteDocumento`, {
            params: { id: idDocumento }
        });
    }


    downloadFile(idFile: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/downloadDocumento`, {
        params: { idFile: idFile },
        responseType: 'blob' // Indica ad Angular di non aspettarsi un JSON
    });
}

    getListaDocumentiByAnimale(idAnimale: string): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/getListaDocumentiPerAnimale/${idAnimale}`);
    }

}