import store from '@/store';
import { debounce, mapOutListeningEvents, isCanceled, isLocked, shouldFireOnEmpty } from './index';

const directives = {
  authorize: (el, binding) => {
    const { value } = binding;
    if (value && !store.getters.checkAuthorization(value)) {
      Promise.resolve().then(() => {
        el.parentNode && el.parentNode.removeChild(el);
      });
    }
  },
  debounce: {
    bind(
      el,
      {
        value: debouncedFn,
        arg: timer = '300ms',
        modifiers,
        listenTo = ['click'],
        lock = false,
        fireOnEmpty = false,
        cancelOnEmpty = false
      }
    ) {
      const combinedRules = Object.assign({ fireOnEmpty, cancelOnEmpty, lock }, modifiers);
      const listener = mapOutListeningEvents(el.attributes, listenTo);
      const fn = debounce(e => {
        debouncedFn(e.target.value, e);
      }, timer);
      function handler(event) {
        if (isCanceled(event.target.value, combinedRules)) {
          fn.cancel();
        } else if (isLocked(event.key, combinedRules) || shouldFireOnEmpty(event.target.value, combinedRules)) {
          fn.cancel();
          debouncedFn(event.target.value, event);
        } else {
          fn(event);
        }
      }
      listener.forEach(e => {
        el.addEventListener(e, handler);
      });
    }
  }
};

export default directives;
