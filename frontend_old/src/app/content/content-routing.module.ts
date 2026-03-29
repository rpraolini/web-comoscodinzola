import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { RicercaComponent } from './ricerca/ricerca.component';
import { LietiComponent } from './lieti/lieti.component';
import { SchedaComponent } from './scheda/scheda.component';
import { LoginComponent } from '../login/login.component';

@NgModule({
  imports: [
    RouterModule.forChild([
      { path: '', component: RicercaComponent},
      { path: 'lieti', component: LietiComponent},
      { path: 'scheda/:id', component: SchedaComponent},
      { path: 'login', component: LoginComponent}
    ])
  ],
  exports: [RouterModule]
})
export class ContentRoutingModule { }
