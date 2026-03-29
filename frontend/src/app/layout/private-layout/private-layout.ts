import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-private-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, ButtonModule],
  templateUrl: './private-layout.html',
  styleUrl: './private-layout.css'
})
export class PrivateLayoutComponent implements OnInit {
  
  username: string = '';
  ruolo: string = '';

  constructor(private authService: AuthService) {}

  ngOnInit() {
    this.username = localStorage.getItem('username') || 'Utente';
    this.ruolo = localStorage.getItem('role') || 'USER';
  }

  logout() {
    this.authService.logout();
  }
}