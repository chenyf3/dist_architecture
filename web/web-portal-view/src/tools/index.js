function convertTime(time) {
  const [amt, t = 'ms'] = String(time).split(/(ms|s)/i);
  const types = {
    ms: 1,
    s: 1000
  };
  return Number(amt) + types[t];
}

function debounce(fn, wait) {
  let timeout = null;
  const timer = typeof wait === 'number' ? wait : convertTime(wait);
  const debounced = function(...args) {
    const later = () => {
      timeout = null;
      fn.apply(this, args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, timer);
    if (!timeout) {
      fn.apply(this.args);
    }
  };
  debounced.cancel = () => {
    clearTimeout(timeout);
    timeout = null;
  };
  return debounced;
}

function toLowerMap(list) {
  return list.map(x => x.toLowerCase());
}

function ensureArray(value) {
  if (Array.isArray(value)) {
    return value;
  }
  if (value === null) {
    return [];
  }
  return [value];
}

function mapOutListeningEvents(attrs, listenTo) {
  const { value = false } = attrs.getNamedItem('debounce-events') || {};
  if (value) {
    return toLowerMap(value.split(','));
  }
  return toLowerMap(ensureArray(listenTo));
}

function isCanceled(inputValue, modifiers) {
  return inputValue === '' && modifiers.cancelOnEmpty;
}

function isLocked(key, modifiers) {
  return key === 'Enter' && (!modifiers.lock || modifiers.unlock);
}

function shouldFireOnEmpty(inputValue, modifiers) {
  return inputValue === '' && modifiers.fireOnEmpty;
}

export { debounce, mapOutListeningEvents, isCanceled, isLocked, shouldFireOnEmpty };
