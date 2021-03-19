import { PostalCodePipe } from './postal-code.pipe';

describe('PostalCodePipe', () => {
  it('create an instance', () => {
    const pipe = new PostalCodePipe();
    expect(pipe).toBeTruthy();
  });
});
