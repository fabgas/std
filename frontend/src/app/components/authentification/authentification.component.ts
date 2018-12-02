import { Component, OnInit } from '@angular/core';
import { AuthentificationService } from '../services/authentification.service';
import { Router } from '@angular/router';
@Component({
  selector: 'app-authentification',
  templateUrl: './authentification.component.html',
  styleUrls: ['./authentification.component.css']
})
export class AuthentificationComponent implements OnInit {
  error = false;
  credentials = {username: '', password: ''};
 title = 'mon jardin';
  constructor(private app: AuthentificationService,  private router: Router) {
  }
  ngOnInit() {
  }

  login() {
    this.app.authenticate(this.credentials , () => {
        this.router.navigateByUrl('/');
    });
    return false;
  }
}
