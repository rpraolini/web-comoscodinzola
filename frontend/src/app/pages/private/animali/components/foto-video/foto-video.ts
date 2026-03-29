import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { DialogModule } from 'primeng/dialog';
import { SelectButtonModule } from 'primeng/selectbutton';
import { ToggleButtonModule } from 'primeng/togglebutton';
import { FotoService } from '../../../../../services/animali/foto.service';
import { TrustUrlPipe } from '../../../../../pipes/trust-url.pipe';

@Component({
  selector: 'app-foto-video',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ButtonModule,
    ToastModule, ConfirmDialogModule, InputTextModule, TagModule,
    TooltipModule, DialogModule, TrustUrlPipe, SelectButtonModule, ToggleButtonModule
  ],
  templateUrl: './foto-video.html',
  providers: [ConfirmationService, MessageService]
})
export class FotoVideo implements OnInit {

  idAnimale!: string;
  foto: any[] = [];
  video: any[] = [];

  // Upload foto
  fileDaCaricare: File | null = null;
  didascalia: string = '';
  uploadingFoto: boolean = false;

  // Lightbox galleria
  mostraGalleria: boolean = false;
  indiceGalleria: number = 0;

  // Aggiunta video
  nuovoVideoUrl: string = '';
  salvandoVideo: boolean = false;

  tipoFotoSelezionato: string = '0';
pubblicaSelezionata: boolean = false;

opzioniTipoFoto = [
  { label: 'Altra foto',        value: '0' },
  { label: 'Foto profilo',      value: '1' },
  { label: 'Foto post-adozione', value: '2' }
];

@ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  constructor(
    private route: ActivatedRoute,
    private fotoService: FotoService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.route.parent?.params.subscribe(params => {
      this.idAnimale = params['id'];
      this.caricaTutto();
    });
  }

  caricaTutto(): void {
    this.fotoService.getFoto(this.idAnimale).subscribe(res => {
      this.foto = res;
      this.cd.detectChanges();
    });
    this.fotoService.getVideo(this.idAnimale).subscribe(res => {
      this.video = res || [];
      this.cd.detectChanges();
    });
  }

onFileInputChange(event: Event): void {
  const input = event.target as HTMLInputElement;
  if (input.files && input.files.length > 0) {
    const file = input.files[0];
    const tipiAccettati = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
    if (!tipiAccettati.includes(file.type)) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Formato non supportato',
        detail: 'Sono accettati solo file immagine (jpg, png, gif, webp)'
      });
      return;
    }
    this.fileDaCaricare = file;
    this.cd.detectChanges();
  }
}

  uploadFoto(): void {
  if (!this.fileDaCaricare) return;
  this.uploadingFoto = true;
  this.fotoService.uploadFoto(
    this.idAnimale,
    this.fileDaCaricare,
    this.didascalia,
    this.tipoFotoSelezionato,
    this.pubblicaSelezionata ? '1' : '0'
  ).subscribe({
    next: () => {
      this.messageService.add({ severity: 'success', summary: 'Caricata', detail: 'Foto aggiunta con successo' });
      this.fileDaCaricare = null;
      this.didascalia = '';
      this.tipoFotoSelezionato = '0';
      this.pubblicaSelezionata = false;
      this.uploadingFoto = false;
       this.fileInput.nativeElement.value = '';
      this.caricaTutto();
    },
    error: () => {
      this.messageService.add({ severity: 'error', summary: 'Errore', detail: 'Impossibile caricare la foto' });
      this.uploadingFoto = false;
    }
  });
}

  impostaProfilo(foto: any): void {
    this.fotoService.impostaProfilo(foto.id_foto).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Aggiornato', detail: 'Foto profilo impostata' });
        this.caricaTutto();
      }
    });
  }

  eliminaFoto(foto: any): void {
    this.confirmationService.confirm({
      message: 'Vuoi eliminare questa foto?',
      header: 'Conferma',
      icon: 'pi pi-trash',
      acceptLabel: 'Sì, elimina',
      rejectLabel: 'Annulla',
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.fotoService.deleteFoto(foto.id_foto).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Eliminata', detail: 'Foto rimossa' });
            this.caricaTutto();
          }
        });
      }
    });
  }

  aggiungiVideo(): void {
    if (!this.nuovoVideoUrl.trim()) return;
    this.salvandoVideo = true;
    const video = { id_animale: this.idAnimale, url: this.nuovoVideoUrl, pubblico: '0' };
    this.fotoService.saveVideo(video).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Aggiunto', detail: 'Video aggiunto con successo' });
        this.nuovoVideoUrl = '';
        this.salvandoVideo = false;
        this.caricaTutto();
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Errore', detail: 'Impossibile aggiungere il video' });
        this.salvandoVideo = false;
      }
    });
  }

  eliminaVideo(video: any): void {
    this.confirmationService.confirm({
      message: 'Vuoi eliminare questo video?',
      header: 'Conferma',
      icon: 'pi pi-trash',
      acceptLabel: 'Sì, elimina',
      rejectLabel: 'Annulla',
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.fotoService.deleteVideo(video.id_video).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Eliminato', detail: 'Video rimosso' });
            this.caricaTutto();
          }
        });
      }
    });
  }

togglePubblica(foto: any): void {
  const nuovoStato = foto.pubblica === '1' ? '0' : '1';
  this.fotoService.togglePubblica(foto.id_foto, nuovoStato).subscribe({
    next: () => {
      foto.pubblica = nuovoStato;
      this.cd.detectChanges();
      this.messageService.add({
        severity: 'info',
        summary: 'Aggiornato',
        detail: nuovoStato === '1' ? 'Foto resa pubblica' : 'Foto resa privata'
      });
    },
    error: (err) => {
      const msg = err.error?.errore || 'Impossibile aggiornare lo stato';
      this.messageService.add({ severity: 'warn', summary: 'Attenzione', detail: msg });
    }
  });
}

  get hasFotoProfilo(): boolean {
    return this.foto.some(f => f.id_tipo_foto === '1');
  }

  apriGalleria(index: number): void {
    this.indiceGalleria = index;
    this.mostraGalleria = true;
  }

  getVideoEmbedUrl(url: string): string {
    // Converte URL YouTube in embed
    const match = url.match(/(?:youtube\.com\/watch\?v=|youtu\.be\/)([^&\n?#]+)/);
    return match ? `https://www.youtube.com/embed/${match[1]}` : url;
  }

  isYoutube(url: string): boolean {
    return url?.includes('youtube') || url?.includes('youtu.be');
  }
}