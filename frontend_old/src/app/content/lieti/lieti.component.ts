import { Component, OnInit } from '@angular/core';
import { ContentService } from '../content.service';


declare var $: any;

@Component({
    selector: 'app-lieti',
    templateUrl: './lieti.component.html',
    standalone: false
})
export class LietiComponent implements OnInit {

  animali: Array<any>;
  anni: Array<any>;
  animaliCount: any;

  constructor(private service: ContentService) { }

  ngOnInit() {
      this.service.getLietiFineCount().subscribe(data => {
        this.animaliCount = data;
      });
      this.service.getLietiFineCountByAnno().subscribe(data => {
        this.anni = data;
      });
      this.service.getLietiFine((new Date()).getFullYear()).subscribe(data => {
        this.animali = data;
      });
  }

  getAnimaliForAnno(arg:string): void {
    this.service.getLietiFine(arg).subscribe(data => {
      this.animali = data;
    });
  }

  showImage(urlImage): void {
    $('body').append('<div class="ui basic modal"><div class="content"><img src="' + urlImage + '" width="100%" /></div></div>');
    $('.ui.modal').modal('show');
  }

}
