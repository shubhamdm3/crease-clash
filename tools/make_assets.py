"""Rebuild the original sound effects and bitmap font. Requires Python + Pillow."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import math, random, struct, wave

ROOT = Path(__file__).resolve().parents[1]
font = ImageFont.truetype(str(ROOT / 'assets/fonts/ui.ttf'), 64)
ascent, descent = font.getmetrics()
atlas = Image.new('RGBA', (1024, 512))
draw = ImageDraw.Draw(atlas)
x = y = 2
row = 0
glyphs = []
for code in range(32, 127):
    char = chr(code)
    left, top, right, bottom = font.getbbox(char, anchor='ls')
    width, height = max(1, right-left), max(1, bottom-top)
    if x + width + 2 > 1024:
        x = 2; y += row + 2; row = 0
    if y+height+2 > 512:
        raise RuntimeError('Font atlas overflow')
    draw.text((x-left, y-top), char, font=font, fill='white', anchor='ls')
    glyphs.append(f'char id={code} x={x} y={y} width={width} height={height} xoffset={left} yoffset={ascent+top} xadvance={round(font.getlength(char))} page=0 chnl=15')
    x += width + 2; row = max(row, height)
atlas.save(ROOT/'assets/fonts/ui.png')
lines = [f'info face="DejaVu Sans" size=64 bold=0 italic=0 charset="" unicode=1 stretchH=100 smooth=1 aa=1 padding=0,0,0,0 spacing=0,0',
         f'common lineHeight={ascent+descent} base={ascent} scaleW=1024 scaleH=512 pages=1 packed=0',
         'page id=0 file="ui.png"', f'chars count={len(glyphs)}', *glyphs, 'kernings count=0']
(ROOT/'assets/fonts/ui.fnt').write_text('\n'.join(lines)+'\n')

random.seed(111)
rate = 22050
for name, duration in [('hit',.12),('bounce',.07),('wicket',.48),('boundary',.65),('release',.06),('run',.12),('win',1.05),('end',.55)]:
    samples = []
    for i in range(int(duration*rate)):
        t = i/rate
        if name == 'hit':
            v = (.68*random.uniform(-1,1)+.32*math.sin(2*math.pi*850*t))*math.exp(-t*55)
        elif name == 'bounce':
            v = math.sin(2*math.pi*160*t)*math.exp(-t*65)*.45
        elif name == 'wicket':
            phase = t % .14
            v = (random.uniform(-1,1)*.65+math.sin(2*math.pi*330*t)*.25)*math.exp(-phase*50)*(1-t/duration)
        elif name in ('boundary','win'):
            notes = [523.25,659.25,783.99,1046.5] if name=='win' else [659.25,783.99,1046.5]
            n = min(len(notes)-1,int(t/duration*len(notes)))
            env = math.sin(math.pi*((t/duration*len(notes))%1))*.24
            v = math.sin(2*math.pi*notes[n]*t)*env
        else:
            freq = 440 if name=='run' else 260 if name=='end' else 190
            v = math.sin(2*math.pi*freq*t)*math.exp(-t*18)*.16
        samples.append(struct.pack('<h',round(max(-1,min(1,v))*25000)))
    with wave.open(str(ROOT/f'assets/audio/{name}.wav'),'wb') as out:
        out.setnchannels(1); out.setsampwidth(2); out.setframerate(rate); out.writeframes(b''.join(samples))
print('Generated bitmap font and 8 original PCM sound effects.')
