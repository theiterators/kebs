---
sidebar_position: 1
title: Introduction to Kebs
---
## Kebs
##### Scala library to eliminate boilerplate
[![Maven Central](https://img.shields.io/maven-central/v/pl.iterators/kebs-slick_2.13.svg)](https://central.sonatype.com/artifact/pl.iterators/kebs-core_2.13)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/theiterators/kebs/master/COPYING)
[![Build Status](https://github.com/theiterators/kebs/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/theiterators/kebs/actions/workflows/ci.yml?query=branch%3Amaster)

![logo](https://raw.githubusercontent.com/theiterators/kebs/master/logo.png)

A library maintained by [Iterators](https://www.iteratorshq.com).

Kebs derives typeclass instances (JSON codecs, database column mappings, HTTP unmarshallers and so on) for your domain types, at compile time.

Kebs 3 supports Scala 2.13 and Scala 3.9 or newer, on JDK 17 or newer. Releases of 3.x are binary compatible with 3.0.0, checked by MiMa. That includes the code the macros generate in your classes: it calls kebs' own runtime, never [hearth](https://github.com/kubuszok/hearth), which is needed at compile time only. Coming from kebs 2? See the [migration guide](migration-3.0.md). Kebs 2 is on the `series/2.x` branch.
