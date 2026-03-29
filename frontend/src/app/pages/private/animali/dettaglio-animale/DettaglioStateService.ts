import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Animale } from '../../../../models/animale.model';


@Injectable()
export class DettaglioStateService {
  private isDirtySource = new BehaviorSubject<boolean>(false);
  isDirty$ = this.isDirtySource.asObservable();
  
  // Il "cuore" del dato: un Subject che tiene l'animale corrente
  private animaleSource = new BehaviorSubject<Animale | null>(null);
  animale$ = this.animaleSource.asObservable();

  setAnimale(animale: Animale) {
    this.animaleSource.next(animale);
  }

  getAnimaleSnapshot(): Animale | null {
    return this.animaleSource.value;
  }

  markAsDirty() {
    if (!this.isDirtySource.value) {
      this.isDirtySource.next(true);
    }
  }

  // Chiamato dopo un salvataggio riuscito
  setClean() {
    this.isDirtySource.next(false);
  }

  getIsDirty(): boolean {
    return this.isDirtySource.value;
  }

}