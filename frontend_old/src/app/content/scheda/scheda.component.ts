import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ContentService } from '../content.service';
import { environment } from '../../../environments/environment';

declare var $: any;

@Component({
    selector: 'app-scheda',
    templateUrl: './scheda.component.html',
    standalone: false
})
export class SchedaComponent implements OnInit {
  env = environment;
  animale: any = {};
  caratteri: Array<any>;
  video: Array<any>;
  foto: Array<any>;
  splitted: Array<any>;
  mail = "";
  contatti = "";
  conf: Array<any>;

  constructor(private router: ActivatedRoute,
    private service: ContentService, private route: Router) { }

  ngOnInit() {
    this.getById();
    this.service.getConf().subscribe(data => {
      this.conf = data;
      this.mail = this.conf.find(e => e.chiave === "mail").descrizione;
      this.contatti = this.conf.find(e => e.chiave === "contatti").descrizione;
    });
  }

  goBack(): void {
    this.route.navigate(['/']);
  }

  getById(): void {
    this.service.getById(this.router.snapshot.paramMap.get('id')).subscribe(data => {
      this.animale = data;
      this.animale.periodo = this.animale.periodo.replace('a', ' anni').replace('m', ' mesi').replace('g', ' giorni');
      this.getCaratteri(this.animale.id_animale);
      this.getFoto(this.animale.id_animale);
      this.getVideo(this.animale.id_animale);
    });
  }

  getCaratteri(id: String): void {
    this.service.getCaratteriById(id).subscribe(data => {
      this.caratteri = data;
    });
  }

  getVideo(id: String): void {
    this.service.getVideoById(id).subscribe(data => {
      this.video = data;
    });
  }

  getFoto(id: String): void {
    this.service.getFotoById(id).subscribe(data => {
      this.foto = data;
    });
  }

  showImage(urlImage): void {
    $('body').append('<div class="ui basic modal"><div class="content"><img src="' + urlImage + '" width="100%" /></div></div>');
    $('.ui.modal').modal('show');
  }

  getVideoId(url): any {
    this.splitted = url.split('/');
    return this.splitted[this.splitted.length - 1];
  }

  getUrl(): any {
    return window.location.href;
  }

}
