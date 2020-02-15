import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-page-title',
  templateUrl: './page-title.component.html'
})
export class PageTitleComponent {
  @Input() title: string;
}
