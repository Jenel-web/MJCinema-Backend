// A small vertical-gradient PNG used as a stand-in for a TMDB poster in the README screenshots.
// (Same idea as web/e2e/png.ts in the frontend repo: no real poster art is ever committed.)
const { deflateSync } = require("node:zlib");

const crcTable = Array.from({ length: 256 }, (_, n) => {
  let c = n;
  for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  return c >>> 0;
});
const crc32 = (buf) => {
  let c = 0xffffffff;
  for (const byte of buf) c = crcTable[(c ^ byte) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
};
const chunk = (type, data) => {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length);
  const body = Buffer.concat([Buffer.from(type), data]);
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crc32(body));
  return Buffer.concat([len, body, crc]);
};

function hslToRgb(h, s, l) {
  const a = s * Math.min(l, 1 - l);
  const f = (n) => {
    const k = (n + h / 30) % 12;
    return Math.round(255 * (l - a * Math.max(-1, Math.min(k - 3, 9 - k, 1))));
  };
  return [f(0), f(8), f(4)];
}

function posterPng(hue, w = 240, h = 360) {
  const top = hslToRgb(hue, 0.7, 0.45);
  const bottom = hslToRgb(hue, 0.5, 0.1);
  const rows = [];
  for (let y = 0; y < h; y++) {
    const t = y / (h - 1);
    const row = Buffer.alloc(1 + w * 3);
    for (let x = 0; x < w; x++) {
      for (let c = 0; c < 3; c++) row[1 + x * 3 + c] = Math.round(top[c] + (bottom[c] - top[c]) * t);
    }
    rows.push(row);
  }
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(w, 0);
  ihdr.writeUInt32BE(h, 4);
  ihdr[8] = 8;
  ihdr[9] = 2;
  return Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    chunk("IHDR", ihdr),
    chunk("IDAT", deflateSync(Buffer.concat(rows))),
    chunk("IEND", Buffer.alloc(0)),
  ]);
}

module.exports = { posterPng };
