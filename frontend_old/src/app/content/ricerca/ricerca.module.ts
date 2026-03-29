import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RicercaComponent } from './ricerca.component';
import { FormsModule } from '@angular/forms';


@NgModule({
  declarations: [RicercaComponent],
  imports: [
    CommonModule,
    FormsModule
  ]
})
export class RicercaModule {  }
