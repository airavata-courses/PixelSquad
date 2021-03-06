# Generated by Django 2.1.5 on 2021-02-27 00:25

from django.db import migrations, models


class Migration(migrations.Migration):

    initial = True

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='Metadata',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('height', models.IntegerField()),
                ('width', models.IntegerField()),
                ('mode', models.CharField(max_length=100)),
                ('formats', models.CharField(max_length=100)),
                ('image', models.BinaryField(blank=True, editable=True, null=True)),
            ],
        ),
    ]
