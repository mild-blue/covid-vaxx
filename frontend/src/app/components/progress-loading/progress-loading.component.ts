import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-progress-loading',
  templateUrl: './progress-loading.component.html',
  styleUrls: ['./progress-loading.component.scss']
})
export class ProgressLoadingComponent {

  @Input() show?: boolean;
}
