import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import {environment} from './../../../environments/environment';
@Injectable({
  providedIn: 'root'
})
export class AuthentificationService {

  authenticated = false;

  constructor(private http: HttpClient) {
  }

  authenticate(credentials, callback) {

        const headers = new HttpHeaders(credentials ? {
            authorization : 'Basic ' + btoa(credentials.username + ':' + credentials.password)
        } : {});
        const url = environment.endPoint + 'user';
        this.http.get(url, {headers: headers}).subscribe(response => {
            if (response['name']) {
                this.authenticated = true;
            } else {
                this.authenticated = false;
            }
            return callback && callback();
        });

    }
    logout(callback) {
      const url = environment.endPoint + 'logout';
      this.http.post('logout', {}).subscribe(() => {
          this.authenticated = false;
          return callback && callback();
      });
    }
}
